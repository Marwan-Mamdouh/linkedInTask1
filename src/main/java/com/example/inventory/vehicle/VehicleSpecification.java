package com.example.inventory.vehicle;

import com.example.inventory.dealer.Dealer;
import com.example.inventory.dealer.SubscriptionType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
class VehicleSpecification {

  public static Specification<Vehicle> build(VehicleFilterCriteria criteria, UUID tenantId) {

    if (criteria == null) {
      return hasTenantId(tenantId);
    }

    if (criteria.priceMin() != null
        && criteria.priceMax() != null
        && criteria.priceMin().compareTo(criteria.priceMax()) > 0) {
      throw new IllegalArgumentException("Invalid price range");
    }

    var spec = hasTenantId(tenantId);
    if (criteria.model() != null) {
      spec = spec.and(hasModel(criteria.model()));
    }
    if (criteria.status() != null) {
      spec = spec.and(hasStatus(criteria.status()));
    }
    if (criteria.priceMin() != null) {
      spec = spec.and(minPrice(criteria.priceMin()));
    }
    if (criteria.priceMax() != null) {
      spec = spec.and(maxPrice(criteria.priceMax()));
    }
    if (criteria.subscription() != null) {
      spec = spec.and(hasSubscription(criteria.subscription()));
    }
    return spec;
  }

  public static Specification<Vehicle> hasTenantId(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  public static Specification<Vehicle> maxPrice(BigDecimal maxPrice) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
  }

  public static Specification<Vehicle> minPrice(BigDecimal minPrice) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
  }

  public static Specification<Vehicle> hasModel(String model) {
    String pattern = "%" + model.trim().toLowerCase() + "%";
    return (root, query, cb) -> cb.like(cb.lower(root.get("model")), pattern);
  }

  public static Specification<Vehicle> hasStatus(VehicleStatus status) {
    return (root, query, cb) -> cb.equal(root.get("vehicleStatus"), status);
  }

  public static Specification<Vehicle> hasSubscription(SubscriptionType subscription) {
    return (root, query, cb) -> {
      if (query != null) {
        query.distinct(true);
      }
      Join<Vehicle, Dealer> dealerJoin = root.join("dealer", JoinType.LEFT);
      return cb.equal(dealerJoin.get("subscriptionType"), subscription);
    };
  }
}
