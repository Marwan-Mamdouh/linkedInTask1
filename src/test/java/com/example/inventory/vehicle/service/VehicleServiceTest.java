package com.example.inventory.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.common.exception.ResourceNotFoundException;
import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.dealer.service.DealerLookupService;
import com.example.inventory.vehicle.domain.Vehicle;
import com.example.inventory.vehicle.domain.VehicleStatus;
import com.example.inventory.vehicle.dto.CreateVehicleRequest;
import com.example.inventory.vehicle.dto.UpdateVehicleRequest;
import com.example.inventory.vehicle.dto.VehicleFilterCriteria;
import com.example.inventory.vehicle.dto.VehicleResponse;
import com.example.inventory.vehicle.repository.VehicleRepository;
import com.example.inventory.vehicle.validator.CreateVehicleValidator;
import com.example.inventory.vehicle.validator.VehicleFilterValidator;
import com.example.tenant.TenantContext;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private DealerLookupService dealerLookupService;
  @Mock private VehicleFilterValidator vehicleFilterValidator;
  @Mock private CreateVehicleValidator createVehicleValidator;

  @InjectMocks private VehicleService vehicleService;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID dealerId = UUID.randomUUID();
  private final UUID vehicleId = UUID.randomUUID();
  private Dealer dealer;
  private Vehicle vehicle;

  @BeforeEach
  void setUp() {
    TenantContext.setTenantId(tenantId);

    dealer = new Dealer();
    dealer.setId(dealerId);
    dealer.setTenantId(tenantId);
    dealer.setName("Test Dealer");

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
        new CreateVehicleRequest(dealerId, "Test Model", new BigDecimal("10000.00"), VehicleStatus.AVAILABLE);

    doNothing().when(createVehicleValidator).validate(request);
    when(dealerLookupService.getDealerOrThrow(dealerId, tenantId)).thenReturn(dealer);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

    VehicleResponse response = vehicleService.save(request);

    assertThat(response.id()).isEqualTo(vehicleId);
    assertThat(response.model()).isEqualTo("Test Model");
    assertThat(response.dealerId()).isEqualTo(dealerId);
    verify(dealerLookupService).getDealerOrThrow(dealerId, tenantId);
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void shouldThrowResourceNotFound_whenCreatingVehicleWithInvalidDealer() {
    CreateVehicleRequest request =
        new CreateVehicleRequest(dealerId, "Test Model", new BigDecimal("10000.00"), VehicleStatus.AVAILABLE);

    doNothing().when(createVehicleValidator).validate(request);
    when(dealerLookupService.getDealerOrThrow(dealerId, tenantId))
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
    when(vehicleRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(vehiclePage);

    var response = vehicleService.getVehicles(criteria, pageable);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().getFirst().model()).isEqualTo("Test Model");
    verify(vehicleRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void shouldUpdateVehicle_whenDealerIdChanged() {
    UUID newDealerId = UUID.randomUUID();
    Dealer newDealer = new Dealer();
    newDealer.setId(newDealerId);
    newDealer.setTenantId(tenantId);

    UpdateVehicleRequest request = new UpdateVehicleRequest(newDealerId, "Updated Model", null, null);

    when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(vehicle));
    when(dealerLookupService.getDealerOrThrow(newDealerId, tenantId)).thenReturn(newDealer);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

    VehicleResponse response = vehicleService.updateVehicle(vehicleId, request);

    assertThat(response.model()).isEqualTo("Updated Model");
    verify(dealerLookupService).getDealerOrThrow(newDealerId, tenantId);
    verify(vehicleRepository).save(vehicle);
  }

  @Test
  void shouldDeleteVehicle_whenValidRequest() {
    doNothing().when(vehicleRepository).deleteByIdAndTenantId(vehicleId, tenantId);

    vehicleService.deleteVehicle(vehicleId);

    verify(vehicleRepository).deleteByIdAndTenantId(vehicleId, tenantId);
  }
}
