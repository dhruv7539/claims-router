package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.exception.InvalidClaimException;
import com.dhruv.claimsrouter.exception.ProviderNotFoundException;
import com.dhruv.claimsrouter.mapper.ProviderMapper;
import com.dhruv.claimsrouter.model.dto.ProviderRequest;
import com.dhruv.claimsrouter.model.dto.ProviderResponse;
import com.dhruv.claimsrouter.model.entity.Provider;
import com.dhruv.claimsrouter.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @Override
    @Transactional
    public ProviderResponse create(ProviderRequest request) {
        if (providerRepository.existsByNpi(request.npi())) {
            throw new InvalidClaimException("Provider with NPI " + request.npi() + " already exists");
        }
        Provider entity = providerMapper.toEntity(request);
        entity.setId(UUID.randomUUID());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        Provider saved = providerRepository.save(entity);
        log.info("Created provider id={} npi={}", saved.getId(), saved.getNpi());
        return providerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse get(UUID id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found: " + id));
        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderResponse> list(Pageable pageable) {
        return providerRepository.findAll(pageable).map(providerMapper::toResponse);
    }
}
