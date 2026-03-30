package com.example.inventory.vehicle;

import com.example.inventory.dealer.Dealer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Entity
@Table(
    indexes = {
      @Index(name = "idx_vehicle_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_vehicle_dealer_id", columnList = "dealer_id"),
      @Index(name = "idx_vehicle_status", columnList = "vehicle_status"),
      @Index(name = "idx_vehicle_tenant_dealer", columnList = "tenant_id, dealer_id"),
      @Index(name = "idx_vehicle_tenant_status", columnList = "tenant_id, vehicle_status"),
      @Index(name = "idx_vehicle_tenant_price", columnList = "tenant_id, price")
    })
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dealer_id", nullable = false)
  private Dealer dealer;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private VehicleStatus vehicleStatus;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();
}
