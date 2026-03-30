package com.example.inventory.vehicle;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository
    extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {
  Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

  void deleteByIdAndTenantId(UUID id, UUID tenantId);
}
