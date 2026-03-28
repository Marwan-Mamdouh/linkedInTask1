package com.example.inventory.vehicle.controller;

import com.example.common.util.PaginatedResponse;
import com.example.inventory.vehicle.dto.CreateVehicleRequest;
import com.example.inventory.vehicle.dto.UpdateVehicleRequest;
import com.example.inventory.vehicle.dto.VehicleFilterCriteria;
import com.example.inventory.vehicle.dto.VehicleResponse;
import com.example.inventory.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  @GetMapping
  public PaginatedResponse<VehicleResponse> getVehicles(
      @ModelAttribute VehicleFilterCriteria criteria,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

    return vehicleService.getVehicles(criteria, pageable);
  }

  @GetMapping("/{id}")
  public ResponseEntity<VehicleResponse> getVehicleByIdAndTenantId(@PathVariable UUID id) {
    return ResponseEntity.ok(vehicleService.getVehicleById(id));
  }

  @PostMapping
  public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest vehicle) {
    return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.save(vehicle));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest request) {
    return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
    vehicleService.deleteVehicle(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
