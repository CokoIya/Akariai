package com.moyz.adi.chat.controller;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.ConfigResp;
import com.moyz.adi.common.dto.ModifyPasswordReq;
import com.moyz.adi.common.dto.UserUpdateReq;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.UserService;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.cat.CatAvatar;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.moyz.adi.common.enums.ErrorEnum.B_IMAGE_LOAD_ERROR;

@Slf4j
@Tag(name = "用户 Controller", description = "用户信息、配置、密码及头像管理")
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取配置信息")
    @GetMapping("/config")
    public ResponseEntity<ConfigResp> getConfig() {
        return ResponseEntity.ok(userService.getConfig());
    }

    @Operation(summary = "更新用户信息")
    @PostMapping("/edit")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserUpdateReq req) {
        userService.updateConfig(req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password/modify")
    public ResponseEntity<String> modifyPassword(@Valid @RequestBody ModifyPasswordReq req) {
        userService.modifyPassword(req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.ok("密码修改成功");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        userService.logout();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取当前用户头像")
    @GetMapping(value = "/myAvatar", produces = MediaType.IMAGE_PNG_VALUE)
    public void myAvatar(HttpServletResponse response) throws IOException {
        var userId = ThreadContext.getCurrentUser().getId();
        writeAvatar(userId, 64, 64, response);
    }

    @Operation(summary = "获取指定用户头像")
    @GetMapping(value = "/avatar/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
    public void avatar(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "64") @Min(32) @Max(128) int size,
            HttpServletResponse response
    ) throws IOException {
        var user = userService.getByUuid(uuid);
        var id = (user != null ? user.getId() : 0L);
        writeAvatar(id, size, size, response);
    }

    private synchronized void writeAvatar(long id, int w, int h, HttpServletResponse resp) throws IOException {
        resp.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + 3600 * 24 * 365);
        var avatar = com.talanlabs.avatargenerator.cat.CatAvatar.newAvatarBuilder().size(w, h).build();
        var img = avatar.create(id);
        ImageIO.write(img, "png", resp.getOutputStream());
    }
}
