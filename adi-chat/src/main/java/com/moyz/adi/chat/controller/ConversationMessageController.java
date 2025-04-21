package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AskReq;
import com.moyz.adi.common.service.ConversationMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "会话消息Controller", description = "处理并管理对话中的消息流与删除")
@RestController
@RequestMapping("/conversation/message")
@Validated
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    @Operation(summary = "发送 prompt 并开启 SSE 流")
    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter process(@Valid @RequestBody AskReq req) {
        return conversationMessageService.sseAsk(req);
    }


    @Operation(summary = "删除消息（软删除）")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> softDelete(@PathVariable String uuid) {
        conversationMessageService.softDelete(uuid);
        return ResponseEntity.noContent().build();
    }

}
