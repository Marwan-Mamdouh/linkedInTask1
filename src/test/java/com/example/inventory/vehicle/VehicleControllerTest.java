package com.example.inventory.vehicle;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.inventory.dealer.Dealer;
import com.example.inventory.dealer.DealerRepository;
import com.example.inventory.dealer.SubscriptionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back DB changes after each test
class VehicleControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DealerRepository dealerRepository;

  @Autowired private VehicleRepository vehicleRepository;

  @Autowired private ObjectMapper objectMapper;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID tenantId2 = UUID.randomUUID();

  private UUID basicDealerId;
  private UUID premiumDealerId;
  private UUID vehicleBasicId;
  private UUID vehiclePremiumId;

  @BeforeEach
  void setUp() {
    // Setup Basic Dealer
    Dealer basicDealer = new Dealer();
    basicDealer.setTenantId(tenantId);
    basicDealer.setName("Basic Dealer");
    basicDealer.setEmail("basic@dealer.com");
    basicDealer.setSubscriptionType(SubscriptionType.BASIC);
    basicDealer = dealerRepository.save(basicDealer);
    basicDealerId = basicDealer.getId();

    // Setup Premium Dealer
    Dealer premiumDealer = new Dealer();
    premiumDealer.setTenantId(tenantId);
    premiumDealer.setName("Premium Dealer");
    premiumDealer.setEmail("premium@dealer.com");
    premiumDealer.setSubscriptionType(SubscriptionType.PREMIUM);
    premiumDealer = dealerRepository.save(premiumDealer);
    premiumDealerId = premiumDealer.getId();

    // Setup Vehicle for Basic Dealer
    Vehicle basicVehicle = new Vehicle();
    basicVehicle.setTenantId(tenantId);
    basicVehicle.setDealer(basicDealer);
    basicVehicle.setModel("Honda Civic");
    basicVehicle.setPrice(new BigDecimal("15000.00"));
    basicVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
    vehicleRepository.save(basicVehicle);
    vehicleBasicId = basicVehicle.getId();

    // Setup Vehicle for Premium Dealer
    Vehicle premiumVehicle = new Vehicle();
    premiumVehicle.setTenantId(tenantId);
    premiumVehicle.setDealer(premiumDealer);
    premiumVehicle.setModel("BMW M3");
    premiumVehicle.setPrice(new BigDecimal("65000.00"));
    premiumVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
    vehicleRepository.save(premiumVehicle);
    vehiclePremiumId = premiumVehicle.getId();
  }

  @Test
  void shouldCreateVehicle_whenValidDealer() throws Exception {
    String payload =
        """
        {
          "dealerId": "%s",
          "model": "Toyota Corolla",
          "price": 20000.00,
          "vehicleStatus": "AVAILABLE"
        }
        """
            .formatted(basicDealerId);

    mockMvc
        .perform(
            post("/api/vehicles")
                .header("X-Tenant-Id", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.model", is("Toyota Corolla")))
        .andExpect(jsonPath("$.dealerId", is(basicDealerId.toString())));
  }

  @Test
  void shouldReturn400_whenCreatingVehicleWithInvalidDealer() throws Exception {
    String payload =
        """
        {
          "dealerId": "%s",
          "model": "Fake Vehicle",
          "price": 10000.00,
          "vehicleStatus": "AVAILABLE"
        }
        """
            .formatted(UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/vehicles")
                .header("X-Tenant-Id", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Validation Failed")));
  }

  @Test
  void shouldGetVehicles_withSubscriptionFilter() throws Exception {
    mockMvc
        .perform(
            get("/api/vehicles")
                .header("X-Tenant-Id", tenantId.toString())
                .param("subscription", "PREMIUM"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].model", is("BMW M3")));
  }

  @Test
  void shouldGetVehicles_withPriceRangeFilter() throws Exception {
    mockMvc
        .perform(
            get("/api/vehicles")
                .header("X-Tenant-Id", tenantId.toString())
                .param("priceMin", "10000")
                .param("priceMax", "20000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].model", is("Honda Civic")));
  }

  @Test
  void invalidTenantHeader_returns400() throws Exception {
    mockMvc
        .perform(get("/api/vehicles"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Invalid or missing X-Tenant-Id")));
  }
}
