package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    @JsonProperty("content")
    private List<T> content;
    
    @JsonProperty("page")
    private int page;
    
    @JsonProperty("size")
    private int size;
    
    @JsonProperty("totalElements")
    private long totalElements;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    @JsonProperty("hasNext")
    private boolean hasNext;
    
    @JsonProperty("hasPrevious")
    private boolean hasPrevious;
    
    @JsonProperty("first")
    private boolean first;
    
    @JsonProperty("last")
    private boolean last;
    
    @JsonProperty("categoryInfo")
    private CategoryInfo categoryInfo;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
    
    public static <T> PageResponse<T> of(List<T> content, Pageable pageable, long total) {
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());
        int currentPage = pageable.getPageNumber();
        
        return PageResponse.<T>builder()
                .content(content)
                .page(currentPage)
                .size(pageable.getPageSize())
                .totalElements(total)
                .totalPages(totalPages)
                .hasNext(currentPage < totalPages - 1)
                .hasPrevious(currentPage > 0)
                .first(currentPage == 0)
                .last(currentPage == totalPages - 1)
                .build();
    }
}