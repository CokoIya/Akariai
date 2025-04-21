package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Prompt Controller", description = "管理用户 Prompt 的增删改查")
@Validated
@RestController
@RequestMapping("/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @Operation(summary = "查询当前用户所有 Prompt")
    @GetMapping("/my/all")
    public ResponseEntity<List<PromptDto>> all() {
        var list = promptService.getAll(ThreadContext.getCurrentUserId());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "按更新时间列出 Prompt")
    @GetMapping("/my/listByUpdateTime")
    public ResponseEntity<PromptListResp> listByUpdate(@RequestParam(required = false) LocalDateTime minUpdateTime) {
        var resp = promptService.listByMinUpdateTime(minUpdateTime);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "分页搜索 Prompt")
    @GetMapping("/my/search")
    public ResponseEntity<Page<PromptDto>> searchPage(
            @RequestParam String keyword,
            @RequestParam @NotNull @Min(1) Integer currentPage,
            @RequestParam @NotNull @Min(1) Integer pageSize
    ) {
        var page = promptService.search(keyword, currentPage, pageSize);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Prompt 自动补全")
    @GetMapping("/my/autocomplete")
    public ResponseEntity<List<PromptDto>> autocomplete(@RequestParam String keyword) {
        var list = promptService.autocomplete(keyword);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "保存多个 Prompt")
    @PostMapping("/save")
    public ResponseEntity<Map<String, Long>> save(@Valid @RequestBody PromptsSaveReq req) {
        var map = promptService.savePrompts(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @Operation(summary = "软删除 Prompt")
    @PostMapping("/del/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promptService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "编辑 Prompt 元数据")
    @PostMapping("/edit/{id}")
    public ResponseEntity<Void> edit(
            @PathVariable Long id,
            @Valid @RequestBody PromptEditReq req
    ) {
        promptService.edit(id, req.getTitle(), req.getRemark());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "全局搜索 Prompt")
    @GetMapping("/search")
    public ResponseEntity<List<PromptDto>> search(@Valid SearchReq req) {
        var list = promptService.search(req.getKeyword());
        return ResponseEntity.ok(list);
    }
}
