package com.example.inventory.vehicle;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateVehicleRequest(
    UUID dealerId, String model, @Positive BigDecimal price, VehicleStatus vehicleStatus) {}
