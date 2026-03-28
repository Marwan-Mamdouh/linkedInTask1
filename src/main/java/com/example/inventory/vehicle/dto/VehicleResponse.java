package com.example.inventory.vehicle.dto;

import com.example.inventory.vehicle.domain.VehicleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponse(
    UUID id,
    UUID dealerId,
    String model,
    BigDecimal price,
    VehicleStatus vehicleStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
