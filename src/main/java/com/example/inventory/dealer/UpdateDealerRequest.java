package com.example.inventory.dealer;

import jakarta.validation.constraints.Email;

public record UpdateDealerRequest(
    String name, @Email String email, SubscriptionType subscriptionType) {}
