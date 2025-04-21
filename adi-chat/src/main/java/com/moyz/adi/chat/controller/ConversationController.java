package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 对话controller
 */
@Tag(name = "对话controller", description = "对话controller")
@RequestMapping("/conversation")
@RestController
@Validated
@RequiredArgsConstructor
public class ConversationController {

    private ConversationService conversationService;

    @Operation(summary = "获取当前用户所有对话")
    @GetMapping("/list")
    public ResponseEntity<List<ConvDto>> list() {
        return ResponseEntity.ok(conversationService.listByUser());
    }


    @Operation(summary = "查询单个对话的信息列表")
    @GetMapping("/{uuid}")
    public ResponseEntity<ConvMsgListResp> detail(
            @PathVariable @NotBlank(message = "uuid 不能为空") String uuid,
            @RequestParam String maxMsgUuid,
            @RequestParam @Min(1) @Max(100) int pageSize
    ) {
        return ResponseEntity.ok(conversationService.detail(uuid, maxMsgUuid, pageSize));
    }

    @Operation(summary = "创建对话")
    @PostMapping("/add")
    public ResponseEntity<ConvDto> add(@Valid @RequestBody ConvAddReq req) {
        var dto = conversationService.add(req.getTitle(), req.getRemark(), req.getAiSystemMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "基于预设创建对话")
    @PostMapping("/addByPreset")
    public ResponseEntity<ConvDto> addByPreset(@RequestParam @Length(min = 32, max = 32) String presetUuid) {
        var dto = conversationService.addByPresetConv(presetUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "编辑对话")
    @PostMapping("/edit/{uuid}")
    public ResponseEntity<Void> edit(
            @PathVariable String uuid,
            @Valid @RequestBody ConvEditReq req
    ) {
        conversationService.edit(uuid, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "删除对话(软删除)")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        conversationService.softDel(uuid);
        return ResponseEntity.noContent().build();
    }
}
