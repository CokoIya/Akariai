package com.moyz.adi.chat.controller;


import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.searchengine.SearchEngineServiceContext;
import com.moyz.adi.common.service.*;
import com.moyz.adi.common.vo.SearchEngineInfo;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.support.CaptchaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Tag(name = "权限controller", description = "用户注册/登录/密码管理")
@Validated
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final String frontendUrl;
    private final UserService userService;

    public AuthController(UserService userService,
                          @Value("${akari.frontend-url}") String frontendUrl) {
        this.userService = userService;
        this.frontendUrl = frontendUrl;
    }

    @Operation(summary = "用户注册")
    @PostMapping(value = "/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterReq req) {
        userService.register(req.getEmail(), req.getPassword(), req.getCaptchaId(), req.getCaptchaCode());
        return ResponseEntity.ok("激活链接已发送，请登录邮箱激活账户");
    }

    @Operation(summary = "注册验证码")
    @GetMapping("/register/captcha")
    public void getRegisterCaptcha(
            @RequestParam @Length(min = 32) String captchaId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        var captcha = HappyCaptcha.require(request, response)
                .type(CaptchaType.WORD_NUMBER_UPPER)
                .build()
                .finish();
        userService.cacheRegisterCaptcha(captchaId, captcha.getCode());
        captcha.output();
    }

    @Operation(summary = "激活用户")
    @GetMapping("active")
    public RedirectView activate(@RequestParam("code") String code) {
        String uri;
        try {
            userService.active(code);
            uri = "/#/active?active=success&msg=" + URLEncoder.encode("激活成功，请登录", StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("激活失败", e);
            String msg = e.getMessage() != null ? e.getMessage() : "系统错误，请重试";
            uri = "/#/active?active=fail&msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        }
        var rv = new RedirectView(frontendUrl + uri);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    @Operation(summary = "忘记密码")
    @PostMapping("password/forgot")
    public ResponseEntity<String> forgotPassword(@RequestParam @NotBlank String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok("重置链接已发送至邮箱");
    }

    @Operation(summary = "重置密码")
    @GetMapping("/password/reset")
    public RedirectView resetPassword(@RequestParam @NotBlank String code) {
        userService.resetPassword(code);
        String uri = "/#/active?active=success&msg=" + URLEncoder.encode("密码已重置", StandardCharsets.UTF_8);
        var rv = new RedirectView(frontendUrl + uri);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    @Operation(summary = "登录")
    @PostMapping("login")
    public ResponseEntity<LoginResp> login(@Valid @RequestBody LoginReq req) {
        var resp = userService.login(req);
        ResponseCookie cookie = ResponseCookie.from(HttpHeaders.AUTHORIZATION, resp.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, resp.getToken())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resp);
    }

    @Operation(summary = "登录验证码")
    @GetMapping("/login/captcha")
    public void getLoginCaptcha(
            @RequestParam @Length(min = 32) String captchaId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        var captcha = HappyCaptcha.require(request, response)
                .type(CaptchaType.WORD_NUMBER_UPPER)
                .build()
                .finish();
        userService.cacheLoginCaptcha(captchaId, captcha.getCode());
        captcha.output();
    }

    @Operation(summary = "搜索引擎列表")
    @GetMapping(value = "/search-engine/list")
    public ResponseEntity<List<SearchEngineInfo>> listSearchEngines() {
        var list = SearchEngineServiceContext.getAllService().values().stream()
                .map(s -> {
                    var info = new SearchEngineInfo();
                    info.setName(s.getEngineName());
                    info.setEnable(s.isEnabled());
                    return info;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
