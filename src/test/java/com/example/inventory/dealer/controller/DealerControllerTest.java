package com.example.inventory.dealer.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.dealer.domain.SubscriptionType;
import com.example.inventory.dealer.repository.DealerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back DB changes after each test
class DealerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DealerRepository dealerRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID tenantId2 = UUID.randomUUID();

  private UUID existingDealerId;

  @BeforeEach
  void setUp() {
    Dealer dealer = new Dealer();
    dealer.setTenantId(tenantId);
    dealer.setName("Setup Dealer");
    dealer.setEmail("setup@dealer.com");
    dealer.setSubscriptionType(SubscriptionType.BASIC);

    dealer = dealerRepository.save(dealer);
    existingDealerId = dealer.getId();

    // Create a dealer in another tenant
    Dealer dealer2 = new Dealer();
    dealer2.setTenantId(tenantId2);
    dealer2.setName("Other Tenant Dealer");
    dealer2.setEmail("other@dealer.com");
    dealer2.setSubscriptionType(SubscriptionType.PREMIUM);
    dealerRepository.save(dealer2);
  }

  @Test
  void shouldCreateDealer_whenValidRequest() throws Exception {
    String payload = """
        {
          "name": "New Dealer",
          "email": "new@dealer.com",
          "subscriptionType": "PREMIUM"
        }
        """;

    mockMvc
        .perform(
            post("/api/dealers")
                .header("X-Tenant-Id", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("New Dealer")))
        .andExpect(jsonPath("$.subscriptionType", is("PREMIUM")));
  }

  @Test
  void shouldGetDealer_whenExists() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers/{id}", existingDealerId)
                .header("X-Tenant-Id", tenantId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(existingDealerId.toString())))
        .andExpect(jsonPath("$.name", is("Setup Dealer")));
  }

  @Test
  void shouldReturn404_whenDealerExistsInAnotherTenant() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers/{id}", existingDealerId)
                .header("X-Tenant-Id", tenantId2.toString())) // Wrong tenant
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("Not Found")));
  }

  @Test
  void shouldReturn404_whenDealerDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers/{id}", UUID.randomUUID())
                .header("X-Tenant-Id", tenantId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldGetDealers_withPaginationAndTenantIsolation() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers")
                .header("X-Tenant-Id", tenantId.toString())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].name", is("Setup Dealer")))
        .andExpect(jsonPath("$.totalItems", is(1)));
  }

  @Test
  void missingTenantHeader_returns400() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Invalid or missing X-Tenant-Id")));
  }

  @Test
  void invalidTenantHeader_returns400() throws Exception {
    mockMvc
        .perform(
            get("/api/dealers")
                .header("X-Tenant-Id", "not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Invalid or missing X-Tenant-Id")));
  }
}
