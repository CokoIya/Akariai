package com.moyz.adi.chat.controller;

import com.moyz.adi.common.service.KnowledgeBaseGraphService;
import com.moyz.adi.common.vo.GraphEdge;
import com.moyz.adi.common.vo.GraphVertex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "知识图谱Controller", description = "知识库图谱顶点与边管理")
@RestController
@RequestMapping("/knowledge-base-graph")
@Validated
@RequiredArgsConstructor
public class KnowledgeBaseGraphController {

    private final KnowledgeBaseGraphService graphService;

    @Operation(summary = "查询图谱数据")
    @GetMapping("/list/{kbItemUuid}")
    public ResponseEntity<Map<String,Object>> list(
            @PathVariable String kbItemUuid,
            @RequestParam(defaultValue = "9223372036854775807") Long maxVertexId,
            @RequestParam(defaultValue = "9223372036854775807") Long maxEdgeId,
            @RequestParam(defaultValue = "-1") int limit) {
        var verts = graphService.listVerticesByKbItemUuid(kbItemUuid, maxVertexId, limit);
        var triples = graphService.listEdgesByKbItemUuid(kbItemUuid, maxEdgeId, limit);
        var pair = graphService.getFromTriple(triples);
        verts.addAll(pair.getLeft());
        var uniqueVerts = verts.stream()
                .collect(Collectors.toMap(GraphVertex::getId, v -> v, (a,b) -> a))
                .values()
                .stream().toList();
        Map<String,Object> body = Map.of(
                "vertices", uniqueVerts,
                "edges", pair.getRight()
        );
        return ResponseEntity.ok(body);
    }
}
