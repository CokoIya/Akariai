package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.KbEditReq;
import com.moyz.adi.common.dto.KbInfoResp;
import com.moyz.adi.common.dto.KbItemIndexBatchReq;
import com.moyz.adi.common.dto.KbSearchReq;
import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.entity.KnowledgeBase;
import com.moyz.adi.common.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Tag(name = "知识库Controller", description = "CRUD 与索引操作")
@Validated
@RestController
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    @Operation(summary = "保存或更新知识库")
    @PostMapping("/saveOrUpdate")
    public ResponseEntity<KnowledgeBase> saveOrUpdate(@Valid @RequestBody KbEditReq req) {
        return ResponseEntity.ok(kbService.saveOrUpdate(req));
    }

    @Operation(summary = "批量上传文档并可选索引")
    @PostMapping(path = "/uploadDocs/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadDocs(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "true") boolean indexAfterUpload,
            @RequestParam(defaultValue = "") String types,
            @RequestPart(required = true) MultipartFile[] docs) {
        List<String> typeList = Arrays.stream(types.split(","))
                .filter(s -> !s.isBlank())
                .toList();
        kbService.uploadDocs(uuid, indexAfterUpload, docs, typeList);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "上传单个文档并可选索引")
    @PostMapping(path = "/upload/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdiFile> upload(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "true") boolean indexAfterUpload,
            @RequestParam(defaultValue = "") String types,
            @RequestPart(required = true) MultipartFile doc) {
        List<String> typeList = Arrays.stream(types.split(","))
                .filter(s -> !s.isBlank())
                .toList();
        AdiFile file = kbService.uploadDoc(uuid, indexAfterUpload, doc, typeList);
        return ResponseEntity.status(HttpStatus.CREATED).body(file);
    }

    @Operation(summary = "搜索我的知识库")
    @GetMapping("/mine/search")
    public ResponseEntity<Page<KbInfoResp>> searchMine(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "false") boolean includeOthersPublic,
            @RequestParam @Min(1) int currentPage,
            @RequestParam @Min(1) int pageSize) {
        Page<KbInfoResp> page = kbService.searchMine(keyword, includeOthersPublic, currentPage, pageSize);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "搜索公开的知识库")
    @GetMapping("/public/search")
    public ResponseEntity<Page<KbInfoResp>> searchPublic(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam @Min(1) int currentPage,
            @RequestParam @Min(1) int pageSize) {
        KbSearchReq req = KbSearchReq.builder().isPublic(true).title(keyword).build();
        Page<KbInfoResp> page = kbService.search(req, currentPage, pageSize);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "获取知识库详情")
    @GetMapping("/info/{uuid}")
    public ResponseEntity<KnowledgeBase> info(@PathVariable String uuid) {
        Optional<KnowledgeBase> kbOpt = Optional.ofNullable(
                kbService.lambdaQuery()
                        .eq(KnowledgeBase::getUuid, uuid)
                        .eq(KnowledgeBase::getIsDeleted, false)
                        .one()
        );
        return kbOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "软删除知识库")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        kbService.softDelete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "索引整个知识库")
    @PostMapping("/indexing/{uuid}")
    public ResponseEntity<Void> indexing(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "") String types) {
        List<String> typeList = Arrays.stream(types.split(","))
                .filter(s -> !s.isBlank())
                .toList();
        kbService.indexing(uuid, typeList);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "批量索引知识点")
    @PostMapping("/item/indexing-list")
    public ResponseEntity<Void> indexItems(@Valid @RequestBody KbItemIndexBatchReq req) {
        kbService.indexItems(Arrays.asList(req.getUuids()), Arrays.asList(req.getIndexTypes()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "检查索引状态")
    @GetMapping("/indexing/check")
    public ResponseEntity<Boolean> checkIndex() {
        boolean finished = kbService.checkIndexIsFinish();
        return ResponseEntity.ok(finished);
    }

    @Operation(summary = "切换收藏状态")
    @PostMapping("/star/toggle")
    public ResponseEntity<Boolean> toggleStar(@RequestParam @NotBlank String kbUuid) {
        boolean result = kbService.toggleStar(ThreadContext.getCurrentUser(), kbUuid);
        return ResponseEntity.ok(result);
    }
}
