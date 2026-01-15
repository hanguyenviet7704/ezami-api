package com.hth.udecareer.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert comma-separated String to List of Long
 * Used for parsing query parameters like categoryIds=5,10,15
 */
@Component
public class StringToLongListConverter implements Converter<String, List<Long>> {
    
    @Override
    public List<Long> convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Arrays.stream(source.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid format for categoryIds. Expected comma-separated Long values (e.g., '5,10,15')", e
            );
        }
    }
}
