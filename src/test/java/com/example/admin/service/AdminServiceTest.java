package com.example.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.common.exception.ValidationException;
import com.example.inventory.dealer.service.DealerLookupService;
import com.example.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock private DealerLookupService dealerLookupService;

  @InjectMocks private AdminService adminService;

  private final UUID tenantId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    TenantContext.setTenantId(tenantId);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void shouldCountDealers_whenScopeIsTenant() {
    Map<String, Long> expectedCounts = Map.of("BASIC", 3L);
    when(dealerLookupService.getDealerSubscriptionCounts(tenantId)).thenReturn(expectedCounts);

    Map<String, Long> result = adminService.countDealersBySubscription("tenant");

    assertThat(result).isEqualTo(expectedCounts);
    verify(dealerLookupService).getDealerSubscriptionCounts(tenantId);
  }

  @Test
  void shouldCountDealers_whenScopeIsNullAndDefaultsToTenant() {
    Map<String, Long> expectedCounts = Map.of("PREMIUM", 1L);
    when(dealerLookupService.getDealerSubscriptionCounts(tenantId)).thenReturn(expectedCounts);

    Map<String, Long> result = adminService.countDealersBySubscription(null);

    assertThat(result).isEqualTo(expectedCounts);
    verify(dealerLookupService).getDealerSubscriptionCounts(tenantId);
  }

  @Test
  void shouldCountDealers_whenScopeIsGlobal() {
    Map<String, Long> expectedCounts = Map.of("BASIC", 10L, "PREMIUM", 5L);
    when(dealerLookupService.getGlobalDealerSubscriptionCounts()).thenReturn(expectedCounts);

    Map<String, Long> result = adminService.countDealersBySubscription("global");

    assertThat(result).isEqualTo(expectedCounts);
    verify(dealerLookupService).getGlobalDealerSubscriptionCounts();
  }

  @Test
  void shouldThrowValidationException_whenScopeIsInvalid() {
    assertThrows(
        ValidationException.class, () -> adminService.countDealersBySubscription("invalid_scope"));
  }
}
