package com.example.inventory.vehicle;

import com.example.inventory.dealer.DealerRepository;
import com.example.tenant.TenantContext;
import jakarta.validation.ValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom validator for CreateVehicleRequest
 *
 * <p>Checks business logic: - Does dealer exist in this tenant? - Is price reasonable (business
 * rule)? - Is status value valid?
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateVehicleValidator {

  private final DealerRepository dealerRepository;

  /** Validate vehicle creation request */
  public void validate(CreateVehicleRequest request) throws ValidationException {
    log.debug("Validating vehicle creation request for dealer: {}", request.dealerId());

    var tenantId = TenantContext.getTenantId();

    // Check 1: Does dealer exist in this tenant?
    validateDealerExists(request.dealerId(), tenantId);

    // Check 2: Is price valid (business logic)?
    validatePrice(request.price());

    // Check 3: Is status value valid?
    validateStatus(request.vehicleStatus().toString());

    log.debug("Vehicle validation passed");
  }

  /**
   * Validate dealer exists and belongs to this tenant
   *
   * <p>This is CRITICAL for multi-tenancy: - Prevents vehicles from being assigned to dealers in
   * other tenants - If you skip this, user A could assign a vehicle to user B's dealer!
   */
  private void validateDealerExists(UUID dealerId, UUID tenantId) {
    dealerRepository
        .findByIdAndTenantId(dealerId, tenantId)
        .orElseThrow(
            () ->
                new ValidationException(
                    String.format(
                        "Dealer with ID '%s' not found in your tenant. "
                            + "Make sure you're assigning vehicles to your own dealers.",
                        dealerId)));
  }

  /**
   * Validate price is reasonable
   *
   * <p>Business rule: Vehicle price must be between $0.01 and $10,000,000 (already checked
   * by @DecimalMin/@DecimalMax, but we do it here too)
   */
  private void validatePrice(java.math.BigDecimal price) {
    if (price == null) {
      throw new ValidationException("Price is required");
    }

    if (price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
      throw new ValidationException(String.format("Price must be greater than 0, got: %s", price));
    }

    if (price.compareTo(new java.math.BigDecimal("9999999.99")) > 0) {
      throw new ValidationException(
          String.format("Price cannot exceed $9,999,999.99, got: %s", price));
    }
  }

  /** Validate status is one of allowed values */
  private void validateStatus(String status) {
    if (status == null) {
      throw new ValidationException("Status is required");
    }

    if (!status.equalsIgnoreCase("AVAILABLE") && !status.equalsIgnoreCase("SOLD")) {
      throw new ValidationException(
          String.format("Invalid status: '%s'. Allowed: AVAILABLE, SOLD", status));
    }
  }
}
