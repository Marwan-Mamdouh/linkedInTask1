package com.example.inventory.dealer;

import java.time.LocalDateTime;
import java.util.UUID;

public record DealerResponse(
    UUID id,
    String name,
    String email,
    SubscriptionType subscriptionType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
