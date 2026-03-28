package com.example.common.exception;

public class CrossTenantAccessException extends RuntimeException {
  public CrossTenantAccessException(String message) {
    super(message);
  }
}
