package com.example.inventory.vehicle.validator;

import com.example.inventory.vehicle.dto.VehicleFilterCriteria;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom validator for VehicleFilterRequest
 *
 * <p>Checks filter logic: - If both priceMin and priceMax are provided, priceMin must be <=
 * priceMax - Prices must be reasonable
 */
@Slf4j
@Component
public class VehicleFilterValidator {

  /** Validate vehicle filter request */
  public void validate(VehicleFilterCriteria filter) throws ValidationException {
    log.debug("Validating vehicle filter request");

    if (filter == null) {
      return; // No filter provided, that's fine
    }

    // Check: If both prices provided, priceMin must be <= priceMax
    validatePriceRange(filter.priceMin(), filter.priceMax());
  }

  /**
   * Validate price range: priceMin <= priceMax
   *
   * <p>This can't be checked with simple annotations because it involves relationship between TWO
   * fields.
   */
  private void validatePriceRange(BigDecimal priceMin, BigDecimal priceMax) {
    // If neither is provided, no validation needed
    if (priceMin == null && priceMax == null) {
      return;
    }

    // If both are provided, check priceMin <= priceMax
    if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
      throw new ValidationException(
          String.format(
              "Invalid price range: priceMin (%s) must be <= priceMax (%s)", priceMin, priceMax));
    }
  }
}
