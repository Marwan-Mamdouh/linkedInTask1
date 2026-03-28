package com.example.admin.controller;

import com.example.admin.service.AdminService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  /**
   * Count dealers by subscription.
   * Default scope is 'tenant'. If scope='global' is requested, it requires
   * GLOBAL_ADMIN role.
   */
  @PreAuthorize("hasRole('GLOBAL_ADMIN')")
  @GetMapping("/dealers/countBySubscription")
  public ResponseEntity<Map<String, Long>> countDealersBySubscription(
      @RequestParam(required = false, defaultValue = "tenant") String scope) {
    return ResponseEntity.ok(adminService.countDealersBySubscription(scope));
  }
}
