package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.workflow.*;
import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.service.WorkflowComponentService;
import com.moyz.adi.common.service.WorkflowService;
import com.moyz.adi.common.workflow.WorkflowStarter;
import com.moyz.adi.common.workflow.node.switcher.OperatorEnum;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Tag(name = "工作流 Controller", description = "流程定义与运行时管理")
@Validated
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowStarter workflowStarter;
    private final WorkflowComponentService componentService;

    @Operation(summary = "创建流程")
    @PostMapping("/add")
    public ResponseEntity<WorkflowResp> addWorkflow(@Valid @RequestBody WfAddReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.add(req.getTitle(), req.getRemark(), req.getIsPublic()));
    }

    @Operation(summary = "复制流程")
    @PostMapping("/copy/{uuid}")
    public ResponseEntity<WorkflowResp> copyWorkflow(@PathVariable String uuid) {
        return ResponseEntity.ok(workflowService.copy(uuid));
    }

    @Operation(summary = "更新流程基础信息")
    @PostMapping("/update/base")
    public ResponseEntity<WorkflowResp> updateBase(@Valid @RequestBody WfBaseInfoUpdateReq req) {
        return ResponseEntity.ok(workflowService.updateBaseInfo(req.getUuid(), req.getTitle(), req.getRemark(), req.getIsPublic()));
    }

    @Operation(summary = "删除/启用/公开流程")
    @PostMapping("/{action}/{uuid}")
    public ResponseEntity<Void> modify(@PathVariable String action,
                                       @PathVariable String uuid,
                                       @RequestParam(defaultValue = "true") Boolean flag) {
        switch (action) {
            case "del" -> workflowService.softDelete(uuid);
            case "enable" -> workflowService.enable(uuid, flag);
            case "public" -> workflowService.setPublic(uuid, flag);
            default -> throw new IllegalArgumentException("Unsupported action");
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "流式运行流程")
    @PostMapping(value = "/run/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter run(@PathVariable String uuid, @Valid @RequestBody WorkflowRunReq req) {
        return workflowStarter.streaming(ThreadContext.getCurrentUser(), uuid, req.getInputs());
    }

    @Operation(summary = "我的流程查询")
    @GetMapping("/mine/search")
    public ResponseEntity<Page<WorkflowResp>> searchMine(
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size) {
        return ResponseEntity.ok(workflowService.search(kw, isPublic, null, page, size));
    }

    @Operation(summary = "公开流程及节点列表")
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicInfo() {
        var ops = OperatorEnum.values();
        var comps = componentService.getAllEnable();
        return ResponseEntity.ok(Map.of("operators", ops, "components", comps));
    }
}
