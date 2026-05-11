package com.dhruv.claimsrouter.controller;

import com.dhruv.claimsrouter.model.dto.ProviderRequest;
import com.dhruv.claimsrouter.model.dto.ProviderResponse;
import com.dhruv.claimsrouter.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Providers", description = "Manage healthcare providers")
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @Operation(summary = "Create a provider")
    @PostMapping
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody ProviderRequest request) {
        ProviderResponse response = providerService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/providers/" + response.id())).body(response);
    }

    @Operation(summary = "Get a provider by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.get(id));
    }

    @Operation(summary = "List providers")
    @GetMapping
    public ResponseEntity<Page<ProviderResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(providerService.list(pageable));
    }
}
