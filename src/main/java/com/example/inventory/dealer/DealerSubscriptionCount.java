package com.example.inventory.dealer;

/** Projection interface for capturing JPQL GROUP BY results safely. */
public interface DealerSubscriptionCount {
  SubscriptionType getSubscriptionType();

  Long getCount();
}
