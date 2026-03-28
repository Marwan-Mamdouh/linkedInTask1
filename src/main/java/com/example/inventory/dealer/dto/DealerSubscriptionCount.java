package com.example.inventory.dealer.dto;

import com.example.inventory.dealer.domain.SubscriptionType;

/**
 * Projection interface for capturing JPQL GROUP BY results safely.
 */
public interface DealerSubscriptionCount {
  SubscriptionType getSubscriptionType();
  Long getCount();
}
