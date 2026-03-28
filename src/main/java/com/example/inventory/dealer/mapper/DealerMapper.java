package com.example.inventory.dealer.mapper;

import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.dealer.dto.CreateDealerRequest;
import com.example.inventory.dealer.dto.DealerResponse;
import com.example.inventory.dealer.dto.UpdateDealerRequest;
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
