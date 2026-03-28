package com.example.inventory.dealer.domain;

import com.example.inventory.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_dealer_tenant_email",
          columnNames = {"tenant_id", "email"})
    },
    indexes = {
      @Index(name = "idx_dealer_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_dealer_email", columnList = "email"),
      @Index(name = "idx_dealer_tenant_name", columnList = "tenant_id, name"),
      @Index(name = "idx_dealer_tenant_subscription", columnList = "tenant_id, subscription_type")
    })
public class Dealer {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String name;

  @Email
  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SubscriptionType subscriptionType;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "dealer", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
  private List<Vehicle> vehicles = new ArrayList<>();
}
