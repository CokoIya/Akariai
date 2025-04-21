package com.moyz.adi.chat.controller;

import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.file.FileOperatorContext;
import com.moyz.adi.common.file.LocalFileUtil;
import com.moyz.adi.common.service.FileService;
import com.moyz.adi.common.util.UrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.moyz.adi.common.cosntant.AdiConstant.IMAGE_EXTENSIONS;
import static com.moyz.adi.common.enums.ErrorEnum.A_FILE_NOT_EXIST;
import static com.moyz.adi.common.enums.ErrorEnum.B_IMAGE_LOAD_ERROR;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;

@Slf4j
@Tag(name = "文件Controller", description = "文件上传下载与删除")
@Validated
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "获取文件或图片流")
    @GetMapping("/file/{uuidWithExt}")
    public ResponseEntity<?> getFile(@PathVariable String uuidWithExt, HttpServletResponse response) throws IOException {
        var uuid = UrlUtil.getUuid(uuidWithExt);
        var file = fileService.getByUuid(uuid);
        if (file == null) throw new BaseException(ErrorEnum.A_FILE_NOT_EXIST);
        if (LocalFileUtil.isImage(file.getExt())) {
            var img = fileService.readMyImage(uuid, false);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000");
            ImageIO.write(img, file.getExt(), response.getOutputStream());
            return null;
        }
        // download non-image
        var bytes = LocalFileUtil.readBytes(file.getPath());
        var resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(
                StringUtils.defaultIfBlank(file.getName(), uuidWithExt)
        ).build());
        headers.setCacheControl("public, max-age=31536000");
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @Operation(summary = "上传文件")
    @PostMapping(path = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> uploadFile(@RequestPart MultipartFile file) {
        var f = fileService.saveFile(file, false);
        return ResponseEntity.ok(Map.of(
                "uuid", f.getUuid(),
                "url", FileOperatorContext.getFileUrl(f)
        ));
    }

    @Operation(summary = "上传图片")
    @PostMapping(path = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> uploadImage(@RequestPart MultipartFile file) {
        var f = fileService.saveFile(file, true);
        return ResponseEntity.ok(Map.of(
                "uuid", f.getUuid(),
                "url", FileOperatorContext.getFileUrl(f)
        ));
    }

    @Operation(summary = "删除文件")
    @PostMapping("/file/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        fileService.removeFileAndSoftDel(uuid);
        return ResponseEntity.noContent().build();
    }
}
