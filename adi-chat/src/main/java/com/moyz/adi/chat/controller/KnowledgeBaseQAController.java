package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.entity.KnowledgeBase;
import com.moyz.adi.common.service.KnowledgeBaseQaRefGraphService;
import com.moyz.adi.common.service.KnowledgeBaseQaService;
import com.moyz.adi.common.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


@Slf4j
@Tag(name = "知识库 QA Controller", description = "问答记录管理与流式响应")
@Validated
@RestController
@RequestMapping("/knowledge-base/qa")
@RequiredArgsConstructor
public class KnowledgeBaseQAController {

    private final KnowledgeBaseService kbService;
    private final KnowledgeBaseQaService qaService;
    private final KnowledgeBaseQaRefGraphService refService;

    @Operation(summary = "新增 QA 记录")
    @PostMapping("/add/{kbUuid}")
    public ResponseEntity<KbQaDto> add(@PathVariable String kbUuid, @Valid @RequestBody QARecordReq req) {
        var kb = kbService.getOrThrow(kbUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(qaService.add(kb, req));
    }

    @Operation(summary = "流式问答过程")
    @PostMapping(value = "/process/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter process(@PathVariable String uuid) {
        return kbService.sseAsk(uuid);
    }

    @Operation(summary = "分页查询 QA")
    @GetMapping("/search")
    public ResponseEntity<Page<KbQaDto>> search(
            @RequestParam String kbUuid,
            @RequestParam String keyword,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size) {
        return ResponseEntity.ok(qaService.search(kbUuid, keyword, page, size));
    }

    @Operation(summary = "软删除 QA")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        qaService.softDelete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取引用 Embedding")
    @GetMapping("/reference/{uuid}")
    public ResponseEntity<List<KbQaRefEmbeddingDto>> references(@PathVariable String uuid) {
        return ResponseEntity.ok(qaService.listReferences(uuid));
    }

    @Operation(summary = "获取引用 Graph")
    @GetMapping("/graph-ref/{uuid}")
    public ResponseEntity<KbQaRefGraphDto> graph(@PathVariable String uuid) {
        return ResponseEntity.ok(refService.getByQaUuid(uuid));
    }

    @Operation(summary = "清空当前用户 QA")
    @PostMapping("/clear")
    public ResponseEntity<Void> clear() {
        qaService.clearByCurrentUser();
        return ResponseEntity.noContent().build();
    }
}
