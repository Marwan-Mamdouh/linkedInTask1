package com.example.inventory.vehicle;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateVehicleRequest(
    @NotNull(message = "Dealer ID is required") UUID dealerId,
    @NotBlank(message = "Vehicle model is required")
        @Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters")
        String model,
    @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @DecimalMax(value = "9999999.99", message = "Price cannot exceed $9,999,999.99")
        BigDecimal price,
    @NotNull(message = "Vehicle status is required") VehicleStatus vehicleStatus) {}
