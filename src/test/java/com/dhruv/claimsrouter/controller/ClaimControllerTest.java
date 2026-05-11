package com.dhruv.claimsrouter.controller;

import com.dhruv.claimsrouter.exception.ClaimNotFoundException;
import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import com.dhruv.claimsrouter.service.ClaimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @Test
    @DisplayName("POST /api/v1/claims returns 201 with response body on success")
    void createClaim() throws Exception {
        ClaimResponse response = sampleResponse(UUID.randomUUID());
        when(claimService.submit(any())).thenReturn(response);

        Map<String, Object> body = Map.of(
                "claimNumber", "CLM-1",
                "patientId", "PAT-1",
                "providerNpi", "1000000001",
                "claimType", "MEDICAL",
                "amount", "250.00",
                "serviceDate", LocalDate.now().minusDays(1).toString(),
                "rawPayload", "{}"
        );

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimNumber").value("CLM-1"))
                .andExpect(jsonPath("$.status").value("ROUTED"));
    }

    @Test
    @DisplayName("POST /api/v1/claims returns 400 when required fields are missing")
    void createClaimValidationFailure() throws Exception {
        Map<String, Object> body = Map.of(
                "claimNumber", "",
                "patientId", "",
                "providerNpi", "abc",
                "amount", "-1",
                "rawPayload", ""
        );

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/claims/{id} returns 200 when found")
    void getFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(claimService.get(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/v1/claims/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/claims/{id} returns 404 when missing")
    void getNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(claimService.get(id)).thenThrow(new ClaimNotFoundException(id));

        mockMvc.perform(get("/api/v1/claims/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private ClaimResponse sampleResponse(UUID id) {
        return new ClaimResponse(
                id,
                "CLM-1",
                "PAT-1",
                UUID.randomUUID(),
                "1000000001",
                "Test Provider",
                ClaimType.MEDICAL,
                new BigDecimal("250.00"),
                LocalDate.now(),
                LocalDateTime.now(),
                ClaimStatus.ROUTED,
                "queue.medical",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
