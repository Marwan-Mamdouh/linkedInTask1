package com.example.inventory.dealer;

import com.example.common.exception.ResourceNotFoundException;
import com.example.common.util.PaginatedResponse;
import com.example.tenant.TenantContext;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class DealerServiceImpl implements DealerService {

  private final DealerRepository dealerRepository;
  private final CreateDealerValidator createDealerValidator;

  @Override
  @Transactional(readOnly = true)
  public DealerResponse getDealerById(UUID id) {
    var tenantId = TenantContext.getTenantId();
    log.info("Get dealer by id request from tenant: {}", tenantId);
    return dealerRepository
        .findByIdAndTenantId(id, tenantId)
        .map(DealerMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public DealerResponse getDealerByEmail(String email) {
    var tenantId = TenantContext.getTenantId();
    log.info("Get dealer by email request from tenant: {}", tenantId);
    return dealerRepository
        .findByEmailAndTenantId(email, tenantId)
        .map(DealerMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with email: " + email));
  }

  @Transactional(readOnly = true)
  public PaginatedResponse<DealerResponse> getAll(Pageable pageable) {
    var tenantId = TenantContext.getTenantId();
    log.info("get page of dealers request from tenant: {}", tenantId);
    var dealers =
        dealerRepository.findAllByTenantId(tenantId, pageable).map(DealerMapper::toResponse);
    return PaginatedResponse.build(dealers);
  }

  @Transactional
  public DealerResponse save(CreateDealerRequest dealer) {
    var tenantId = TenantContext.getTenantId();
    log.info("create dealer request from tenant: {}", tenantId);
    createDealerValidator.validate(dealer);
    var dealerEntity = DealerMapper.toEntity(dealer);
    dealerEntity.setTenantId(tenantId);
    return DealerMapper.toResponse(dealerRepository.save(dealerEntity));
  }

  @Transactional
  public DealerResponse updateDealerRequest(UUID id, UpdateDealerRequest updateDealerRequest) {
    var tenantId = TenantContext.getTenantId();

    log.info("update dealer request from tenant: {}", tenantId);
    var savedDealer =
        dealerRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + id));

    DealerMapper.updateEntity(savedDealer, updateDealerRequest);
    return DealerMapper.toResponse(dealerRepository.save(savedDealer));
  }

  @Transactional
  public void deleteDealer(UUID uuid) {
    var tenantId = TenantContext.getTenantId();
    log.info("delete dealer request from tenant: {}", tenantId);
    this.getDealerById(uuid);
    dealerRepository.deleteByIdAndTenantId(uuid, tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Long> getDealerSubscriptionCounts(UUID tenantId) {
    var counts = dealerRepository.countDealersBySubscription(tenantId);
    return counts.stream()
        .collect(
            Collectors.toMap(
                delaer -> delaer.getSubscriptionType().toString(),
                DealerSubscriptionCount::getCount));
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Long> getGlobalDealerSubscriptionCounts() {
    var counts = dealerRepository.countAllDealersBySubscription();
    return counts.stream()
        .collect(
            Collectors.toMap(
                delaer -> delaer.getSubscriptionType().toString(),
                DealerSubscriptionCount::getCount));
  }
}
