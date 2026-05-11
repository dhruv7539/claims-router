package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.dto.ProviderRequest;
import com.dhruv.claimsrouter.model.dto.ProviderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProviderService {

    ProviderResponse create(ProviderRequest request);

    ProviderResponse get(UUID id);

    Page<ProviderResponse> list(Pageable pageable);
}
