package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.workflow.WfRuntimeNodeDto;
import com.moyz.adi.common.dto.workflow.WfRuntimeResp;
import com.moyz.adi.common.dto.workflow.WorkflowResumeReq;
import com.moyz.adi.common.service.WorkflowRuntimeService;
import com.moyz.adi.common.workflow.WorkflowStarter;
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

import java.util.List;

@Slf4j
@Tag(name = "工作流运行时 Controller", description = "运行时数据查询与操作")
@Validated
@RestController
@RequestMapping("/workflow/runtime")
@RequiredArgsConstructor
public class WorkflowRuntimeController {

    private final WorkflowRuntimeService runtimeService;
    private final WorkflowStarter starter;

    @Operation(summary = "继续执行流程")
    @PostMapping("/resume/{uuid}")
    public ResponseEntity<Void> resume(@PathVariable String uuid, @Valid @RequestBody WorkflowResumeReq req) {
        starter.resumeFlow(uuid, req.getFeedbackContent());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "查询运行时记录")
    @GetMapping("/page")
    public ResponseEntity<Page<WfRuntimeResp>> page(
            @RequestParam String wfUuid,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size) {
        return ResponseEntity.ok(runtimeService.page(wfUuid, page, size));
    }

    @Operation(summary = "查询节点详情")
    @GetMapping("/nodes/{uuid}")
    public ResponseEntity<List<WfRuntimeNodeDto>> nodes(@PathVariable String uuid) {
        return ResponseEntity.ok(runtimeService.listByRuntimeUuid(uuid));
    }

    @Operation(summary = "清空或删除运行时")
    @PostMapping("/{action}")
    public ResponseEntity<Void> action(@RequestParam(required = false) String wfUuid, @PathVariable String action) {
        boolean ok = switch (action) {
            case "clear" -> runtimeService.deleteAll(wfUuid);
            case "del" -> runtimeService.softDelete(wfUuid);
            default -> throw new IllegalArgumentException();
        };
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }
}
