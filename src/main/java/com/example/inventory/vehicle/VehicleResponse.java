package com.example.inventory.vehicle;

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
