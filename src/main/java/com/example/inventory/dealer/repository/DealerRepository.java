package com.example.inventory.dealer.repository;

import com.example.inventory.dealer.domain.Dealer;
import com.example.inventory.dealer.dto.DealerSubscriptionCount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, UUID> {

  Optional<Dealer> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<Dealer> findByEmailAndTenantId(String email, UUID tenantId);

  Page<Dealer> findAllByTenantId(UUID tenantId, Pageable pageable);

  /** Count dealers by subscription type Returns a Map<SubscriptionType, Count> */
  @Query(
      """
        SELECT d.subscriptionType as subscriptionType, COUNT(d) as count
        FROM Dealer d
        WHERE d.tenantId = :tenantId
        GROUP BY d.subscriptionType
    """)
  List<DealerSubscriptionCount> countDealersBySubscription(@Param("tenantId") UUID tenantId);

  /** Global count of dealers by subscription type across ALL tenants */
  @Query(
      """
        SELECT d.subscriptionType as subscriptionType, COUNT(d) as count
        FROM Dealer d
        GROUP BY d.subscriptionType
    """)
  List<DealerSubscriptionCount> countAllDealersBySubscription();

  void deleteByIdAndTenantId(UUID id, UUID tenantId);
}
