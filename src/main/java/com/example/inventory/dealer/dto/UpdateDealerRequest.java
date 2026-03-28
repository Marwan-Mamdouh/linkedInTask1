package com.example.inventory.dealer.dto;

import com.example.inventory.dealer.domain.SubscriptionType;
import jakarta.validation.constraints.Email;

public record UpdateDealerRequest(
        String name, @Email String email, SubscriptionType subscriptionType) {
}
