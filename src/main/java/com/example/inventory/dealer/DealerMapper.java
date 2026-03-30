package com.example.inventory.dealer;

import java.time.LocalDateTime;

public class DealerMapper {
  public static Dealer toEntity(CreateDealerRequest request) {
    Dealer dealer = new Dealer();
    dealer.setName(request.name());
    dealer.setEmail(request.email());
    dealer.setSubscriptionType(request.subscriptionType());
    dealer.setCreatedAt(LocalDateTime.now());
    dealer.setUpdatedAt(LocalDateTime.now());
    return dealer;
  }

  public static Dealer toEntity(DealerResponse response) {
    var dealer = new Dealer();
    dealer.setId(response.id());
    dealer.setName(response.name());
    dealer.setEmail(response.email());
    dealer.setSubscriptionType(response.subscriptionType());
    dealer.setUpdatedAt(response.updatedAt());
    return dealer;
  }

  public static void updateEntity(Dealer dealer, UpdateDealerRequest request) {
    if (request.name() != null) {
      dealer.setName(request.name());
    }
    if (request.email() != null) {
      dealer.setEmail(request.email());
    }
    if (request.subscriptionType() != null) {
      dealer.setSubscriptionType(request.subscriptionType());
    }
    dealer.setUpdatedAt(LocalDateTime.now());
  }

  public static DealerResponse toResponse(Dealer dealer) {
    return new DealerResponse(
        dealer.getId(),
        dealer.getName(),
        dealer.getEmail(),
        dealer.getSubscriptionType(),
        dealer.getCreatedAt(),
        dealer.getUpdatedAt());
  }
}
