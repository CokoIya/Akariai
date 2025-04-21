package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.ConvPresetSearchReq;
import com.moyz.adi.common.entity.ConversationPreset;
import com.moyz.adi.common.service.ConversationPresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "预设会话Controller", description = "搜索与关联预设会话")
@Validated
@RestController
@RequestMapping("/conversation-preset")
@RequiredArgsConstructor
public class ConversationPresetController {

    private final ConversationPresetService presetService;


    @Operation(summary = "搜索预设会话(角色)")
    @PostMapping("/search")
    public ResponseEntity<Page<ConversationPreset>> search(
            @Valid @RequestBody ConvPresetSearchReq req,
            @RequestParam @NotNull @Min(1) Integer currentPage,
            @RequestParam @NotNull @Min(1) Integer pageSize) {
        var page = presetService.search(req.getTitle(), currentPage, pageSize);
        return ResponseEntity.ok(page);
    }
}
