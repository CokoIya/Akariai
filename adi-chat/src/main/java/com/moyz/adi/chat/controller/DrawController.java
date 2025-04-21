package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.DrawService;
import com.moyz.adi.common.service.FileService;
import com.moyz.adi.common.util.UrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static com.moyz.adi.common.enums.ErrorEnum.*;

/**
 * 绘图
 */
@Slf4j
@Tag(name = "绘图Controller", description = "管理AI绘图任务与文件")
@Validated
@RestController
@RequestMapping("/draw")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;
    private final FileService fileService;

    @Operation(summary = "图片生成")
    @PostMapping("/generation")
    public ResponseEntity<Map<String, String>> generation(@Valid @RequestBody GenerateImageReq req) {
        var uuid = drawService.createByPrompt(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("uuid", uuid));
    }

    @Operation(summary = "重新生成")
    @PostMapping("/regenerate/{uuid}")
    public ResponseEntity<Void> regenerate(@PathVariable @Length(min = 32, max = 32) String uuid) {
        drawService.regenerate(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "编辑图片")
    @PostMapping("/edit")
    public ResponseEntity<Map<String, String>> edit(@Valid @RequestBody EditImageReq req) {
        var uuid = drawService.editByOriginalImage(req);
        return ResponseEntity.ok(Map.of("uuid", uuid));
    }

    @Operation(summary = "图片变体")
    @PostMapping("/variation")
    public ResponseEntity<Map<String, String>> variation(@Valid @RequestBody VariationImageReq req) {
        var uuid = drawService.variationImage(req);
        return ResponseEntity.ok(Map.of("uuid", uuid));
    }

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public ResponseEntity<DrawListResp> list(
            @RequestParam Long maxId,
            @RequestParam int pageSize) {
        return ResponseEntity.ok(drawService.listByCurrentUser(maxId, pageSize));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/detail/{uuid}")
    public ResponseEntity<DrawDto> detail(@PathVariable String uuid) {
        var dto = drawService.getPublicOrMine(uuid);
        if (dto == null) throw new BaseException(ErrorEnum.A_DRAW_NOT_FOUND);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "切换公开状态")
    @PostMapping("/set-public/{uuid}")
    public ResponseEntity<DrawDto> setPublic(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "false") Boolean isPublic,
            @RequestParam(required = false) Boolean withWatermark) {
        return ResponseEntity.ok(drawService.setDrawPublic(uuid, isPublic, withWatermark));
    }

    @Operation(summary = "删除任务")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        drawService.del(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "删除单文件")
    @PostMapping("/file/del/{fileUuid}")
    public ResponseEntity<Void> deleteFile(
            @RequestParam @NotBlank String uuid,
            @PathVariable String fileUuid) {
        drawService.delGeneratedFile(uuid, fileUuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "公开图片")
    @GetMapping(value = "/public/image/{drawUuid}/{imageUuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void publicImage(@Length(min = 32) @PathVariable String drawUuid, @Length(min = 32, max = 32) @PathVariable String imageUuidWithExt, HttpServletResponse response) {
        DrawDto drawDto = drawService.getPublicOrMine(drawUuid);
        if (null == drawDto) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        String imageUuid = UrlUtil.getUuid(imageUuidWithExt);
        BufferedImage bufferedImage = fileService.readImage(imageUuid, false);
        //把图片写给浏览器
        try {
            ImageIO.write(bufferedImage, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("publicImage error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    @Operation(summary = "公开缩略图")
    @GetMapping(value = "/public/thumbnail/{drawUuid}/{imageUuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void publicThumbnail(@Length(min = 32) @PathVariable String drawUuid, @Length(min = 32) @PathVariable String imageUuidWithExt, HttpServletResponse response) {
        DrawDto drawDto = drawService.getPublicOrMine(drawUuid);
        if (null == drawDto) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        String imageUuid = UrlUtil.getUuid(imageUuidWithExt);
        BufferedImage bufferedImage = fileService.readImage(imageUuid, true);
        try {
            ImageIO.write(bufferedImage, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("publicThumbnail error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }
}
