package com.dhruv.claimsrouter.integration;

import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.entity.RoutingDecision;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.DecisionOutcome;
import com.dhruv.claimsrouter.repository.ClaimRepository;
import com.dhruv.claimsrouter.repository.RoutingDecisionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test that boots the full Spring context, posts a claim against
 * the seeded routing rules, and verifies that the claim got routed and an
 * audit row was written.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimRoutingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private RoutingDecisionRepository decisionRepository;

    @Test
    @DisplayName("POST /api/v1/claims routes a high-value medical claim and writes an audit record")
    void submitAndRouteHighValueMedicalClaim() throws Exception {
        Map<String, Object> body = Map.of(
                "claimNumber", "CLM-IT-1",
                "patientId", "PAT-IT-1",
                "providerNpi", "1000000001",
                "claimType", "MEDICAL",
                "amount", "75000.00",
                "serviceDate", LocalDate.now().minusDays(2).toString(),
                "rawPayload", "{\"src\":\"integration-test\"}"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ROUTED"))
                .andExpect(jsonPath("$.routingDestination").value("queue.medical.manual-review"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID claimId = UUID.fromString(json.get("id").asText());

        Claim persisted = claimRepository.findById(claimId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ClaimStatus.ROUTED);
        assertThat(persisted.getRoutingDestination()).isEqualTo("queue.medical.manual-review");

        List<RoutingDecision> decisions = decisionRepository.findByClaimIdOrderByDecisionAtDesc(claimId);
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getOutcome()).isEqualTo(DecisionOutcome.ROUTED);
    }

    @Test
    @DisplayName("Pharmacy west region claim routes to the regional queue")
    void submitPharmacyWest() throws Exception {
        Map<String, Object> body = Map.of(
                "claimNumber", "CLM-IT-2",
                "patientId", "PAT-IT-2",
                "providerNpi", "1000000005",
                "claimType", "PHARMACY",
                "amount", "150.00",
                "serviceDate", LocalDate.now().minusDays(1).toString(),
                "rawPayload", "{\"src\":\"integration-test\"}"
        );

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routingDestination").value("queue.pharmacy.west"));
    }

    @Test
    @DisplayName("Bigdecimal amount over decimal-max returns 400 from validation")
    @SuppressWarnings("unused")
    void rejectsOverlyLargeAmount() throws Exception {
        Map<String, Object> body = Map.of(
                "claimNumber", "CLM-IT-3",
                "patientId", "PAT-IT-3",
                "providerNpi", "1000000001",
                "claimType", "MEDICAL",
                "amount", new BigDecimal("99999999.99"),
                "serviceDate", LocalDate.now().minusDays(1).toString(),
                "rawPayload", "{}"
        );

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
