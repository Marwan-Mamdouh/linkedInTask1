package com.example.inventory.vehicle.service;

import com.example.common.util.PaginatedResponse;
import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.dealer.service.DealerLookupService;
import com.example.inventory.vehicle.domain.Vehicle;
import com.example.inventory.vehicle.dto.CreateVehicleRequest;
import com.example.inventory.vehicle.dto.UpdateVehicleRequest;
import com.example.inventory.vehicle.dto.VehicleFilterCriteria;
import com.example.inventory.vehicle.dto.VehicleResponse;
import com.example.inventory.vehicle.mapper.VehicleMapper;
import com.example.inventory.vehicle.repository.VehicleRepository;
import com.example.inventory.vehicle.spec.VehicleSpecification;
import com.example.inventory.vehicle.validator.CreateVehicleValidator;
import com.example.inventory.vehicle.validator.VehicleFilterValidator;
import com.example.common.exception.ResourceNotFoundException;
import com.example.tenant.TenantContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final DealerLookupService dealerLookupService;
  private final VehicleFilterValidator vehicleFilterValidator;
  private final CreateVehicleValidator createVehicleValidator;

  @Transactional(readOnly = true)
  public PaginatedResponse<VehicleResponse> getVehicles(
      VehicleFilterCriteria criteria, Pageable pageable) {

    UUID tenantId = TenantContext.getTenantId();
    log.info("List vehicles for tenant: {}", tenantId);
    vehicleFilterValidator.validate(criteria);

    var spec = VehicleSpecification.build(criteria, tenantId);

    Page<Vehicle> vehicles = vehicleRepository.findAll(spec, pageable);
    return PaginatedResponse.build(vehicles.map(VehicleMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public VehicleResponse getVehicleById(UUID id) {
    var tenantId = TenantContext.getTenantId();
    log.info("Get vehicle by ID with Tenant ID: {}", tenantId);
    return vehicleRepository
        .findByIdAndTenantId(id, tenantId)
        .map(VehicleMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
  }

  @Transactional
  public VehicleResponse save(CreateVehicleRequest request) {
    var tenantId = TenantContext.getTenantId();
    log.info("Create Vehicle with Tenant ID: {}", tenantId);

    createVehicleValidator.validate(request);

    Dealer dealer = dealerLookupService.getDealerOrThrow(request.dealerId(), tenantId);

    return VehicleMapper.toResponse(
        vehicleRepository.save(VehicleMapper.toEntity(request, dealer, tenantId)));
  }

  @Transactional
  public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {
    var tenantId = TenantContext.getTenantId();
    log.info("Update Vehicle with Tenant ID: {}", tenantId);

    var vehicle = this.vehicleRepository
        .findByIdAndTenantId(id, tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Vehicle not found with id: " + id));

    // Resolve dealer only if the patch request includes a new dealerId
    Dealer dealer = null;
    if (request.dealerId() != null) {
      dealer = dealerLookupService.getDealerOrThrow(request.dealerId(), tenantId);
    }

    VehicleMapper.updateEntity(vehicle, request, dealer);
    return VehicleMapper.toResponse(vehicleRepository.save(vehicle));
  }

  @Transactional
  public void deleteVehicle(UUID id) {
    var tenantId = TenantContext.getTenantId();
    log.info("Delete Vehicle with Tenant ID: {}", tenantId);
    vehicleRepository.deleteByIdAndTenantId(id, tenantId);
  }
}
