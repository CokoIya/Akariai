package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.KbItemDto;
import com.moyz.adi.common.dto.KbItemEditReq;
import com.moyz.adi.common.entity.KnowledgeBaseItem;
import com.moyz.adi.common.service.KnowledgeBaseItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Tag(name = "知识库条目 Controller", description = "条目增删改查")
@Validated
@RestController
@RequestMapping("/knowledge-base-item")
@RequiredArgsConstructor
public class KnowledgeBaseItemController {

    private final KnowledgeBaseItemService service;

    @Operation(summary = "保存或更新条目")
    @PostMapping("/saveOrUpdate")
    public ResponseEntity<KnowledgeBaseItem> save(@Valid @RequestBody KbItemEditReq req) {
        return ResponseEntity.ok(service.saveOrUpdate(req));
    }

    @Operation(summary = "分页搜索条目")
    @GetMapping("/search")
    public ResponseEntity<Page<KbItemDto>> search(
            @RequestParam String kbUuid,
            @RequestParam String keyword,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size) {
        return ResponseEntity.ok(service.search(kbUuid, keyword, page, size));
    }

    @Operation(summary = "获取条目详情")
    @GetMapping("/info/{uuid}")
    public ResponseEntity<KnowledgeBaseItem> info(@PathVariable String uuid) {
        var item = service.lambdaQuery()
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .eq(KnowledgeBaseItem::getIsDeleted, false)
                .one();
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "软删除条目")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        service.softDelete(uuid);
        return ResponseEntity.noContent().build();
    }
}
