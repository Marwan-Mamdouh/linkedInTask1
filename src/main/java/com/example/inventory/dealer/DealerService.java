package com.example.inventory.dealer;

import java.util.Map;
import java.util.UUID;

/**
 * Contract for other modules (like Vehicle or Admin) to safely look up Dealer entities and
 * aggregated data without coupling directly to the DealerRepository.
 */
public interface DealerService {
  DealerResponse getDealerById(UUID dealerId);

  Map<String, Long> getDealerSubscriptionCounts(UUID tenantId);

  Map<String, Long> getGlobalDealerSubscriptionCounts();
}
