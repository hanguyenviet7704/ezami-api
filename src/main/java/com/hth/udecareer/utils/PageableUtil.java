package com.hth.udecareer.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;

public class PageableUtil {
    
    private PageableUtil() {}
    
    public static boolean isPaginationRequested(Integer page, Integer size) {
        return page != null || size != null;
    }
    
    public static <T> Page<T> listToPage(List<T> list, Pageable pageable) {
        if (list == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        if (start > list.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }
        
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}