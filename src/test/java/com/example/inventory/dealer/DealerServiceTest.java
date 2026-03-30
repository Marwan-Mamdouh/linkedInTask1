package com.example.inventory.dealer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.common.exception.ResourceNotFoundException;
import com.example.tenant.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DealerServiceTest {

  @Mock private DealerRepository dealerRepository;
  @Mock private CreateDealerValidator createDealerValidator;

  @InjectMocks private DealerServiceImpl dealerService;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID dealerId = UUID.randomUUID();
  private Dealer dealer;

  @BeforeEach
  void setUp() {
    TenantContext.setTenantId(tenantId);

    dealer = new Dealer();
    dealer.setId(dealerId);
    dealer.setTenantId(tenantId);
    dealer.setName("Test Dealer");
    dealer.setEmail("test@dealer.com");
    dealer.setSubscriptionType(SubscriptionType.BASIC);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void shouldCreateDealer_whenValidInput() {
    CreateDealerRequest request =
        new CreateDealerRequest("Test Dealer", "test@dealer.com", SubscriptionType.BASIC);

    when(dealerRepository.save(any(Dealer.class))).thenReturn(dealer);
    doNothing().when(createDealerValidator).validate(request);

    DealerResponse response = dealerService.save(request);

    assertThat(response.id()).isEqualTo(dealerId);
    assertThat(response.name()).isEqualTo("Test Dealer");
    assertThat(response.email()).isEqualTo("test@dealer.com");
    verify(dealerRepository).save(any(Dealer.class));
  }

  @Test
  void shouldGetDealer_whenExists() {
    when(dealerRepository.findByIdAndTenantId(dealerId, tenantId)).thenReturn(Optional.of(dealer));

    DealerResponse response = dealerService.getDealerById(dealerId);

    assertThat(response.id()).isEqualTo(dealerId);
    verify(dealerRepository).findByIdAndTenantId(dealerId, tenantId);
  }

  @Test
  void shouldThrowResourceNotFound_whenDealerNotFound() {
    when(dealerRepository.findByIdAndTenantId(dealerId, tenantId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> dealerService.getDealerById(dealerId));
    verify(dealerRepository).findByIdAndTenantId(dealerId, tenantId);
  }

  @Test
  void shouldUpdateDealer_whenValidInput() {
    UpdateDealerRequest request =
        new UpdateDealerRequest("New Name", "new@dealer.com", SubscriptionType.PREMIUM);

    when(dealerRepository.findByIdAndTenantId(dealerId, tenantId)).thenReturn(Optional.of(dealer));
    when(dealerRepository.save(any(Dealer.class))).thenReturn(dealer);

    DealerResponse response = dealerService.updateDealerRequest(dealerId, request);

    assertThat(response.name()).isEqualTo("New Name");
    assertThat(response.subscriptionType()).isEqualTo(SubscriptionType.PREMIUM);
    verify(dealerRepository).save(dealer);
  }

  @Test
  void shouldDeleteDealer_whenValidRequest() {
    when(dealerRepository.findByIdAndTenantId(dealerId, tenantId)).thenReturn(Optional.of(dealer));
    doNothing().when(dealerRepository).deleteByIdAndTenantId(dealerId, tenantId);

    dealerService.deleteDealer(dealerId);

    verify(dealerRepository).deleteByIdAndTenantId(dealerId, tenantId);
  }

  @Test
  void shouldCountDealersBySubscription() {
    DealerSubscriptionCount countBasic = mock(DealerSubscriptionCount.class);
    when(countBasic.getSubscriptionType()).thenReturn(SubscriptionType.BASIC);
    when(countBasic.getCount()).thenReturn(5L);

    when(dealerRepository.countDealersBySubscription(tenantId)).thenReturn(List.of(countBasic));

    Map<String, Long> counts = dealerService.getDealerSubscriptionCounts(tenantId);

    assertThat(counts).containsEntry(SubscriptionType.BASIC.name(), 5L).hasSize(1);
    verify(dealerRepository).countDealersBySubscription(tenantId);
  }
}
