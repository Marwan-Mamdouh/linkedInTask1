package com.example.inventory.dealer;

import com.example.tenant.TenantContext;
import jakarta.validation.ValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class CreateDealerValidator {

  private final DealerRepository dealerRepository;

  /**
   * Validate dealer creation request
   *
   * <p>This method is called AFTER simple validation (@NotNull, @Email, etc.) passes. Now we check
   * business logic.
   *
   * @param request The validated request (annotations already checked)
   * @throws ValidationException If business logic validation fails
   */
  public void validate(CreateDealerRequest request) throws ValidationException {
    log.debug("Validating dealer creation request: {}", request.email());

    var tenantId = TenantContext.getTenantId();

    // Check 1: Is email already used in this tenant?
    validateEmailUniqueness(request.email(), tenantId);

    // Check 2: Is subscription type valid?
    validateSubscriptionType(request.subscriptionType().toString());

    log.debug("Dealer validation passed");
  }

  /**
   * Validate email is unique per tenant
   *
   * <p>This requires a DATABASE query (can't be done with simple annotations)
   */
  private void validateEmailUniqueness(String email, UUID tenantId) {
    dealerRepository
        .findByEmailAndTenantId(email, tenantId)
        .ifPresent(
            existing -> {
              // Email already exists in this tenant
              throw new ValidationException(
                  String.format(
                      "Email '%s' is already used in your tenant. Each email must be unique.",
                      email));
            });
  }

  /**
   * Validate subscription type is one of allowed values
   *
   * <p>Could also load from database if subscription types are dynamic
   */
  private void validateSubscriptionType(String subscriptionType) {
    if (!subscriptionType.equalsIgnoreCase("BASIC")
        && !subscriptionType.equalsIgnoreCase("PREMIUM")) {
      throw new ValidationException(
          String.format(
              "Invalid subscription type: '%s'. Allowed: BASIC, PREMIUM", subscriptionType));
    }
  }
}
