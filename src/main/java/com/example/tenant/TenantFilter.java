package com.example.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class TenantFilter implements Filter {

  private static final String HEADER = "X-Tenant-Id";
  private static final String ERROR_MSG = "Invalid or missing X-Tenant-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    var httpRequest = (HttpServletRequest) request;
    String path = httpRequest.getRequestURI();
    // Skip Swagger and actuator endpoints
    if (path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/actuator")) {
      chain.doFilter(request, response);
      return;
    }

    var httpResponse = (HttpServletResponse) response;

    var tenantHeader = httpRequest.getHeader(HEADER);

    try {
      if (tenantHeader == null || tenantHeader.isBlank()) {
        sendErrorResponse(httpResponse, ERROR_MSG);
        return;
      }

      UUID tenantId;

      try {
        tenantId = UUID.fromString(tenantHeader);
      } catch (IllegalArgumentException e) {
        sendErrorResponse(httpResponse, ERROR_MSG);
        return;
      }
      TenantContext.setTenantId(tenantId);

      // Continue request
      chain.doFilter(request, response);

    } finally {
      // avoid memory leaks
      TenantContext.clear();
    }
  }

  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("application/json;charset=UTF-8");

    String jsonResponse = String.format("{\"error\": \"%s\", \"status\": 400}", message);
    if (!response.isCommitted()) {
      response.getWriter().write(jsonResponse);
      response.getWriter().flush();
    }
  }
}
