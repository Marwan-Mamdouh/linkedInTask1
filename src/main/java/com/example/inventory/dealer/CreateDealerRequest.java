package com.example.inventory.dealer;

import jakarta.validation.constraints.*;

public record CreateDealerRequest(
    @NotBlank(message = "Dealer name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
    @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid (e.g., user@example.com)")
        String email,
    @NotNull(message = "Subscription type is required") SubscriptionType subscriptionType) {}
