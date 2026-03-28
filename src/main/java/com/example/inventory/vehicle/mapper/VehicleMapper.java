package com.example.inventory.vehicle.mapper;

import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.vehicle.domain.Vehicle;
import com.example.inventory.vehicle.dto.CreateVehicleRequest;
import com.example.inventory.vehicle.dto.UpdateVehicleRequest;
import com.example.inventory.vehicle.dto.VehicleResponse;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class VehicleMapper {

  public static Vehicle toEntity(CreateVehicleRequest request, Dealer dealer, UUID tenantId) {
    Vehicle vehicle = new Vehicle();
    vehicle.setTenantId(tenantId);
    vehicle.setDealer(dealer);
    vehicle.setModel(request.model());
    vehicle.setPrice(request.price());
    vehicle.setVehicleStatus(request.vehicleStatus());
    return vehicle;
  }

  public static void updateEntity(Vehicle vehicle, UpdateVehicleRequest request, Dealer dealer) {
    if (dealer != null) {
      vehicle.setDealer(dealer);
    }
    if (request.model() != null) {
      vehicle.setModel(request.model());
    }
    if (request.price() != null) {
      vehicle.setPrice(request.price());
    }
    if (request.vehicleStatus() != null) {
      vehicle.setVehicleStatus(request.vehicleStatus());
    }
    vehicle.setUpdatedAt(LocalDateTime.now());
  }

  public static VehicleResponse toResponse(Vehicle vehicle) {
    return new VehicleResponse(
        vehicle.getId(),
        vehicle.getDealer().getId(),
        vehicle.getModel(),
        vehicle.getPrice(),
        vehicle.getVehicleStatus(),
        vehicle.getCreatedAt(),
        vehicle.getUpdatedAt());
  }
}
