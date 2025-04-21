package com.moyz.adi.chat.controller;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.ConvPresetRelDto;
import com.moyz.adi.common.service.ConversationPresetRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "预设会话关联Controller", description = "获取用户使用的预设会话")
@Validated
@RestController
@RequestMapping("/conversation-preset-rel")
@RequiredArgsConstructor
public class ConversationPresetRelController {

    private final ConversationPresetRelService relService;

    @Operation(summary = "获取当前用户使用的预设会话")
    @GetMapping("/mine")
    public ResponseEntity<List<ConvPresetRelDto>> mine(
            @RequestParam(defaultValue = "100") Integer limit) {
        var userId = ThreadContext.getCurrentUserId();
        return ResponseEntity.ok(relService.listByUser(userId, limit));
    }
}
