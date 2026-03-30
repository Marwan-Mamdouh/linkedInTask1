package com.example.inventory.vehicle;

import com.example.inventory.dealer.SubscriptionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record VehicleFilterCriteria(
    @Size(min = 2, max = 100, message = "Model filter must be between 2 and 100 characters")
        String model,
    @Pattern(regexp = "^(AVAILABLE|SOLD)$", message = "Status must be AVAILABLE or SOLD")
        VehicleStatus status,
    @DecimalMin(value = "0", message = "Minimum price must be >= 0") BigDecimal priceMin,
    @DecimalMax(value = "9999999.99", message = "Maximum price cannot exceed $9,999,999.99")
        BigDecimal priceMax,
    @Pattern(regexp = "^(BASIC|PREMIUM)$", message = "Subscription must be BASIC or PREMIUM")
        SubscriptionType subscription // for PREMIUM filter
    ) {}
