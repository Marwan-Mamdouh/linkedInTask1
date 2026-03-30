package com.example.inventory.vehicle;

import com.example.common.exception.ResourceNotFoundException;
import com.example.common.util.PaginatedResponse;
import com.example.inventory.dealer.DealerMapper;
import com.example.inventory.dealer.DealerResponse;
import com.example.inventory.dealer.DealerService;
import com.example.tenant.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final DealerService dealerLookupService;
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

  public VehicleResponse save(CreateVehicleRequest request) {
    var tenantId = TenantContext.getTenantId();
    log.info("Create Vehicle with Tenant ID: {}", tenantId);

    createVehicleValidator.validate(request);

    var dealer = dealerLookupService.getDealerById(request.dealerId());

    return VehicleMapper.toResponse(
        vehicleRepository.save(
            VehicleMapper.toEntity(request, DealerMapper.toEntity(dealer), tenantId)));
  }

  public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {
    var tenantId = TenantContext.getTenantId();
    log.info("Update Vehicle with Tenant ID: {}", tenantId);

    var vehicle = this.vehicleRepository
        .findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

    // Resolve dealer only if the patch request includes a new dealerId
    DealerResponse dealer = null;
    if (request.dealerId() != null) {
      dealer = dealerLookupService.getDealerById(request.dealerId());
    }

    VehicleMapper.updateEntity(vehicle, request, DealerMapper.toEntity(dealer));
    return VehicleMapper.toResponse(vehicleRepository.save(vehicle));
  }

  public void deleteVehicle(UUID id) {
    var tenantId = TenantContext.getTenantId();
    log.info("Delete Vehicle with Tenant ID: {}", tenantId);

    this.getVehicleById(id);
    vehicleRepository.deleteByIdAndTenantId(id, tenantId);
  }
}
