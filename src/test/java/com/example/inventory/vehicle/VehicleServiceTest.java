package com.example.inventory.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.common.exception.ResourceNotFoundException;
import com.example.inventory.dealer.Dealer;
import com.example.inventory.dealer.DealerMapper;
import com.example.inventory.dealer.DealerResponse;
import com.example.inventory.dealer.DealerService;
import com.example.tenant.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private DealerService dealerLookupService;
  @Mock private VehicleFilterValidator vehicleFilterValidator;
  @Mock private CreateVehicleValidator createVehicleValidator;

  @InjectMocks private VehicleService vehicleService;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID dealerId = UUID.randomUUID();
  private final UUID vehicleId = UUID.randomUUID();
  private Dealer dealer;
  private DealerResponse dealerResponse;
  private Vehicle vehicle;

  @BeforeEach
  void setUp() {
    TenantContext.setTenantId(tenantId);

    dealer = new Dealer();
    dealer.setId(dealerId);
    dealer.setTenantId(tenantId);
    dealer.setName("Test Dealer");

    dealerResponse = DealerMapper.toResponse(dealer);

    vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setTenantId(tenantId);
    vehicle.setDealer(dealer);
    vehicle.setModel("Test Model");
    vehicle.setPrice(new BigDecimal("10000.00"));
    vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void shouldCreateVehicle_whenValidDealer() {
    CreateVehicleRequest request =
        new CreateVehicleRequest(
            dealerId, "Test Model", new BigDecimal("10000.00"), VehicleStatus.AVAILABLE);

    doNothing().when(createVehicleValidator).validate(request);
    when(dealerLookupService.getDealerById(dealerId)).thenReturn(dealerResponse);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

    VehicleResponse response = vehicleService.save(request);

    assertThat(response.id()).isEqualTo(vehicleId);
    assertThat(response.model()).isEqualTo("Test Model");
    assertThat(response.dealerId()).isEqualTo(dealerId);
    verify(dealerLookupService).getDealerById(dealerId);
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void shouldThrowResourceNotFound_whenCreatingVehicleWithInvalidDealer() {
    CreateVehicleRequest request =
        new CreateVehicleRequest(
            dealerId, "Test Model", new BigDecimal("10000.00"), VehicleStatus.AVAILABLE);

    doNothing().when(createVehicleValidator).validate(request);
    when(dealerLookupService.getDealerById(dealerId))
        .thenThrow(new ResourceNotFoundException("Dealer not found"));

    assertThrows(ResourceNotFoundException.class, () -> vehicleService.save(request));
    verify(vehicleRepository, never()).save(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldGetVehicles_withFilters() {
    VehicleFilterCriteria criteria = new VehicleFilterCriteria(null, null, null, null, null);
    Pageable pageable = Pageable.unpaged();
    Page<Vehicle> vehiclePage = new PageImpl<>(List.of(vehicle));

    doNothing().when(vehicleFilterValidator).validate(criteria);
    when(vehicleRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(vehiclePage);

    var response = vehicleService.getVehicles(criteria, pageable);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().getFirst().model()).isEqualTo("Test Model");
    verify(vehicleRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void shouldUpdateVehicle_whenDealerIdChanged() {
    UUID newDealerId = UUID.randomUUID();
    var newDealer = new DealerResponse(newDealerId, null, null, null, null, null);

    UpdateVehicleRequest request =
        new UpdateVehicleRequest(newDealerId, "Updated Model", null, null);

    when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId))
        .thenReturn(Optional.of(vehicle));
    when(dealerLookupService.getDealerById(newDealerId)).thenReturn(newDealer);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

    VehicleResponse response = vehicleService.updateVehicle(vehicleId, request);

    assertThat(response.model()).isEqualTo("Updated Model");
    verify(dealerLookupService).getDealerById(newDealerId);
    verify(vehicleRepository).save(vehicle);
  }

  @Test
  void shouldDeleteVehicle_whenValidRequest() {
    doNothing().when(vehicleRepository).deleteByIdAndTenantId(vehicleId, tenantId);

    vehicleService.deleteVehicle(vehicleId);

    verify(vehicleRepository).deleteByIdAndTenantId(vehicleId, tenantId);
  }
}
