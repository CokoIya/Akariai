package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AiSearchReq;
import com.moyz.adi.common.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Tag(name = "AI Search Controller", description = "流式搜索及查询记录")
@Validated
@RestController
@RequestMapping("/ai-search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "SSE 流式搜索")
    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSearch(@Valid @RequestBody AiSearchReq req) {
        return searchService.search(req.isBriefSearch(), req.getSearchText(), req.getEngineName(), req.getModelName());
    }
}
