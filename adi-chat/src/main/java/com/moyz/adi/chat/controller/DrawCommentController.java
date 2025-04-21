package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.DrawCommentDto;
import com.moyz.adi.common.service.DrawCommentService;
import com.moyz.adi.common.service.DrawService;
import com.moyz.adi.common.vo.DrawCommentAddReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "绘图评论Controller", description = "管理绘图评论分页与操作")
@Validated
@RestController
@RequestMapping("/draw/comment")
@RequiredArgsConstructor
public class DrawCommentController {

    private final DrawService drawService;
    private final DrawCommentService commentService;

    @Operation(summary = "分页获取评论")
    @GetMapping("/list/{uuid}")
    public ResponseEntity<Page<DrawCommentDto>> list(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        var resp = drawService.listCommentsByPage(uuid, page, size);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "新增评论")
    @PostMapping("/add")
    public ResponseEntity<DrawCommentDto> add(@Valid @RequestBody DrawCommentAddReq req) {
        var dto = drawService.addComment(req.getDrawUuid(), req.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "删除评论(软删除)")
    @PostMapping("/del")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        commentService.softDel(id);
        return ResponseEntity.noContent().build();
    }
}
