package com.dhruv.claimsrouter.mapper;

import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(target = "providerId", source = "provider.id")
    @Mapping(target = "providerNpi", source = "provider.npi")
    @Mapping(target = "providerName", source = "provider.name")
    ClaimResponse toResponse(Claim claim);
}
