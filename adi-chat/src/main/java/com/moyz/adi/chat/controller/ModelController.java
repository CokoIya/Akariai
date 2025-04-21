package com.moyz.adi.chat.controller;

import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.helper.ImageModelContext;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.vo.ImageModelInfo;
import com.moyz.adi.common.vo.LLMModelInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


@Slf4j
@Tag(name = "模型 Controller", description = "支持的 LLM 与图片模型列表")
@Validated
@RestController
@RequestMapping("/model")
public class ModelController {
    @Operation(summary = "支持的大语言模型列表")
    @GetMapping("/llms")
    public ResponseEntity<List<LLMModelInfo>> llms() {
        var list = LLMContext.getAllServices().values().stream()
                .map(svc -> {
                    var m = svc.getAiModel();
                    return LLMModelInfo.builder()
                            .modelId(m.getId())
                            .modelName(m.getName())
                            .modelTitle(m.getTitle())
                            .modelPlatform(m.getPlatform())
                            .enable(m.getIsEnable())
                            .inputTypes(Collections.singletonList(m.getInputTypes()))
                            .isFree(m.getIsFree())
                            .build();
                })
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "支持的图片模型列表")
    @GetMapping("/imageModels")
    public ResponseEntity<List<ImageModelInfo>> imageModels() {
        var list = ImageModelContext.NAME_TO_LLM_SERVICE.values().stream()
                .map(svc -> {
                    var m = svc.getAiModel();
                    return ImageModelInfo.builder()
                            .modelId(m.getId())
                            .modelName(m.getName())
                            .modelTitle(m.getTitle())
                            .modelPlatform(m.getPlatform())
                            .enable(m.getIsEnable())
                            .isFree(m.getIsFree())
                            .build();
                })
                .toList();
        return ResponseEntity.ok(list);
    }
}
