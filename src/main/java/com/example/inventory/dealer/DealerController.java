package com.example.inventory.dealer;

import com.example.common.util.PaginatedResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
class DealerController {

  private final DealerServiceImpl dealerService;

  @GetMapping("/{id}")
  public ResponseEntity<DealerResponse> getDealerByIdAndTenantId(@PathVariable UUID id) {
    return ResponseEntity.ok(dealerService.getDealerById(id));
  }

  @GetMapping("/by-email")
  public ResponseEntity<DealerResponse> getDealerByEmailAndTenantId(@RequestParam String email) {
    return ResponseEntity.ok(dealerService.getDealerByEmail(email));
  }

  @GetMapping
  public PaginatedResponse<DealerResponse> getAllByTenantId(Pageable pageable) {
    return dealerService.getAll(pageable);
  }

  @PostMapping
  public ResponseEntity<DealerResponse> create(@Valid @RequestBody CreateDealerRequest dealer) {
    return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.save(dealer));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<DealerResponse> updateDaler(
      @PathVariable UUID id, @Valid @RequestBody UpdateDealerRequest request) {
    return ResponseEntity.ok(dealerService.updateDealerRequest(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
    dealerService.deleteDealer(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
