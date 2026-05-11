package com.dhruv.claimsrouter.controller;

import com.dhruv.claimsrouter.model.dto.RoutingRuleRequest;
import com.dhruv.claimsrouter.model.dto.RoutingRuleResponse;
import com.dhruv.claimsrouter.service.RoutingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Routing Rules", description = "Manage routing rules used by the routing engine")
@RestController
@RequestMapping("/api/v1/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    @Operation(summary = "Create a routing rule")
    @PostMapping
    public ResponseEntity<RoutingRuleResponse> create(@Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse response = routingRuleService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/routing-rules/" + response.id())).body(response);
    }

    @Operation(summary = "List routing rules sorted by priority (lowest first)")
    @GetMapping
    public ResponseEntity<List<RoutingRuleResponse>> list() {
        return ResponseEntity.ok(routingRuleService.listAllByPriority());
    }

    @Operation(summary = "Deactivate a routing rule")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RoutingRuleResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(routingRuleService.deactivate(id));
    }
}
