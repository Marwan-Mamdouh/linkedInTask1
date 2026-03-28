package com.example.admin.service;

import com.example.common.exception.ValidationException;
import com.example.inventory.dealer.service.DealerLookupService;
import com.example.tenant.TenantContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

  private final DealerLookupService dealerLookupService;

  public Map<String, Long> countDealersBySubscription(String scope) {
    if (scope == null || "tenant".equalsIgnoreCase(scope)) {
      var tenantId = TenantContext.getTenantId();
      log.info("Count dealers by subscription (Tenant Scope) for tenant: {}", tenantId);
      return dealerLookupService.getDealerSubscriptionCounts(tenantId);
    } 
    
    if ("global".equalsIgnoreCase(scope)) {
      log.info("Count dealers by subscription (Global Scope)");
      return dealerLookupService.getGlobalDealerSubscriptionCounts();
    }
    
    throw new ValidationException("Invalid scope parameter. Supported values: tenant, global");
  }
}
