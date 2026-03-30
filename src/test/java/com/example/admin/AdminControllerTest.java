package com.example.admin;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.inventory.dealer.Dealer;
import com.example.inventory.dealer.DealerRepository;
import com.example.inventory.dealer.SubscriptionType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back DB changes after each test
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DealerRepository dealerRepository;

  private final UUID tenantId1 = UUID.randomUUID();
  private final UUID tenantId2 = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // Tenant 1 has 2 BASIC dealers and 1 PREMIUM dealer
    for (int i = 0; i < 2; i++) {
      createDealer(tenantId1, "T1 Basic " + i, SubscriptionType.BASIC);
    }
    createDealer(tenantId1, "T1 Premium", SubscriptionType.PREMIUM);

    // Tenant 2 has 3 PREMIUM dealers
    for (int i = 0; i < 3; i++) {
      createDealer(tenantId2, "T2 Premium " + i, SubscriptionType.PREMIUM);
    }
  }

  private void createDealer(UUID tenantId, String name, SubscriptionType type) {
    Dealer dealer = new Dealer();
    dealer.setTenantId(tenantId);
    dealer.setName(name);
    dealer.setEmail(name.replace(" ", "").toLowerCase() + "@dealer.com");
    dealer.setSubscriptionType(type);
    dealerRepository.save(dealer);
  }

  @Test
  @WithMockUser(roles = "GLOBAL_ADMIN")
  void shouldCountDealers_perTenantByDefault() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/dealers/countBySubscription")
                .header("X-Tenant-Id", tenantId1.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.BASIC", is(2)))
        .andExpect(jsonPath("$.PREMIUM", is(1)));
  }

  @Test
  @WithMockUser(roles = "USER") // Regular user with no special admin role
  void shouldNotAllowRegularUser_toCountOwnTenantDealers() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/dealers/countBySubscription")
                .header("X-Tenant-Id", tenantId1.toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "GLOBAL_ADMIN") // Spring automatically applies "ROLE_GLOBAL_ADMIN"
  void shouldCountDealers_globally_whenAdminRoleIsPresent() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/dealers/countBySubscription")
                .header("X-Tenant-Id", tenantId1.toString())
                .param("scope", "global"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.BASIC", is(2))) // Only Tenant 1 has Basic
        .andExpect(jsonPath("$.PREMIUM", is(4))); // Tenant 1 (1) + Tenant 2 (3) = 4
  }

  @Test
  void shouldReturn403_whenScopeIsGlobalButNoRole() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/dealers/countBySubscription")
                .header("X-Tenant-Id", tenantId1.toString())
                .param("scope", "global"))
        .andExpect(status().isForbidden()); // Security configured to block non `GLOBAL_ADMIN`
  }
}
