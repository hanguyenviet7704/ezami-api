package com.hth.udecareer.model.request;

import com.hth.udecareer.exception.InvalidPaginationException;
import lombok.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Value
public class PaginationRequest {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    
    int page;
    int size;
    Sort sort;
    
    private PaginationRequest(Integer page, Integer size, Sort sort) {
        this.page = page != null ? page : DEFAULT_PAGE;
        this.size = size != null ? size : DEFAULT_SIZE;
        this.sort = sort;
        validate();
    }
    
    public static PaginationRequest of(Integer page, Integer size) {
        return new PaginationRequest(page, size, null);
    }
    
    public static PaginationRequest of(Integer page, Integer size, String sortStr) {
        Sort sort = parseSortString(sortStr);
        return new PaginationRequest(page, size, sort);
    }
    
    public Pageable toPageable() {
        if (sort != null) {
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size);
    }
    
    private void validate() {
        if (page < 0) {
            throw new InvalidPaginationException("Page index must not be less than zero");
        }
        if (size < 1) {
            throw new InvalidPaginationException("Page size must be at least 1");
        }
        if (size > MAX_SIZE) {
            throw new InvalidPaginationException("Page size must not exceed " + MAX_SIZE);
        }
    }
    
    private static Sort parseSortString(String sortStr) {
        if (sortStr == null || sortStr.trim().isEmpty()) {
            return null;
        }
        String[] parts = sortStr.split(",");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String direction = parts[1].trim();
            if ("desc".equalsIgnoreCase(direction)) {
                return Sort.by(Sort.Direction.DESC, field);
            }
        }
        return Sort.by(Sort.Direction.ASC, parts[0].trim());
    }
}