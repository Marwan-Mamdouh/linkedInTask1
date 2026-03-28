package com.example.common.util;

import java.util.List;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class PaginatedResponse<T> {
  private List<T> data;
  private int currentPage;
  private int totalPages;
  private long totalItems;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;

  private PaginatedResponse() {}

  public static <T> PaginatedResponse<T> build(Page<T> page) {
    var response = new PaginatedResponse<T>();
    response.setData(page.getContent());
    response.setCurrentPage(page.getNumber());
    response.setTotalPages(page.getTotalPages());
    response.setTotalItems(page.getTotalElements());
    response.setPageSize(page.getSize());
    response.setHasNext(page.hasNext());
    response.setHasPrevious(page.hasPrevious());
    return response;
  }
}
