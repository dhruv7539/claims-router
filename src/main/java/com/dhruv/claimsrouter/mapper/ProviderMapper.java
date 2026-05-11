package com.dhruv.claimsrouter.mapper;

import com.dhruv.claimsrouter.model.dto.ProviderRequest;
import com.dhruv.claimsrouter.model.dto.ProviderResponse;
import com.dhruv.claimsrouter.model.entity.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProviderMapper {

    ProviderResponse toResponse(Provider provider);

    @Mapping(target = "id", ignore = true)
    Provider toEntity(ProviderRequest request);
}
