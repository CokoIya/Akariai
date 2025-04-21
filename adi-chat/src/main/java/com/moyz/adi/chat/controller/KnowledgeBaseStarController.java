package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.KbStarInfoResp;
import com.moyz.adi.common.service.KnowledgeBaseStarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "知识库收藏 Controller", description = "管理用户的知识库收藏列表")
@Validated
@RestController
@RequestMapping("/knowledge-base/star")
@RequiredArgsConstructor
public class KnowledgeBaseStarController {

    private final KnowledgeBaseStarService starService;

    @Operation(summary = "获取我的知识库收藏分页列表")
    @GetMapping("/mine")
    public ResponseEntity<Page<KbStarInfoResp>> mine(
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size
    ) {
        var result = starService.listStarInfo(ThreadContext.getCurrentUserId(), page, size);
        return ResponseEntity.ok(result);
    }
}
