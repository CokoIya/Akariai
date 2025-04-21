package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AiSearchResp;
import com.moyz.adi.common.service.AiSearchRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Tag(name = "AI Search Record Controller", description = "查询及删除搜索记录")
@Validated
@RestController
@RequestMapping("/ai-search-record")
@RequiredArgsConstructor
public class SearchRecordController {

    private final AiSearchRecordService recordService;

    @Operation(summary = "分页查询搜索记录")
    @GetMapping("/list")
    public ResponseEntity<AiSearchResp> listRecords(
            @RequestParam(defaultValue = "0") Long maxId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(recordService.listByMaxId(maxId, keyword));
    }

    @Operation(summary = "软删除记录")
    @PostMapping("/del/{uuid}")
    public ResponseEntity<Void> deleteRecord(@PathVariable String uuid) {
        recordService.softDelete(uuid);
        return ResponseEntity.noContent().build();
    }
}
