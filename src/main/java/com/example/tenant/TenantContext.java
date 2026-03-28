package com.example.tenant;

import java.util.UUID;

/**
 * Holds the current tenant ID throughout the request lifecycle.
 *
 * <p>Uses ThreadLocal for thread-safety: - Each thread/request has its own tenant ID - No
 * cross-request contamination - Perfect for servlet containers with thread pools
 */
public class TenantContext {

  // ThreadLocal = each thread gets its own storage
  // Thread 1 (Tenant A) and Thread 2 (Tenant B) don't interfere
  private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();

  /** Store the current tenant ID Called by interceptor when request arrives */
  public static void setTenantId(UUID tenantId) {
    TENANT.set(tenantId);
  }

  /** Retrieve the current tenant ID Called by services/repositories during request processing */
  public static UUID getTenantId() {
    UUID tenantId = TENANT.get();

    // Safety check - should never happen if interceptor is set up correctly
    if (tenantId == null) {
      throw new IllegalStateException("Tenant ID is not set");
    }
    return tenantId;
  }

  /** Check if tenant ID is set (useful for optional scenarios) */
  public static boolean hasTenantId() {
    return TENANT.get() != null;
  }

  /**
   * Clear the tenant ID from ThreadLocal IMPORTANT: Called after request completes to prevent
   * memory leaks
   *
   * <p>Without this, in app servers with thread pools: Thread 1 finishes request for Tenant A
   * Thread 1 is reused for request from Tenant B But tenantIdHolder still has Tenant A! →
   * Cross-tenant data leak!
   */
  public static void clear() {
    TENANT.remove();
  }
}
