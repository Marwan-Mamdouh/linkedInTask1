package com.example.inventory.dealer.service;

import com.example.inventory.dealer.domain.Dealer;

import java.util.Map;
import java.util.UUID;

/**
 * Contract for other modules (like Vehicle or Admin) to safely look up Dealer entities
 * and aggregated data without coupling directly to the DealerRepository.
 */
public interface DealerLookupService {
  Dealer getDealerOrThrow(UUID dealerId, UUID tenantId);

  Map<String, Long> getDealerSubscriptionCounts(UUID tenantId);

  Map<String, Long> getGlobalDealerSubscriptionCounts();
}
