package com.hth.udecareer.controllers;

import com.hth.udecareer.model.dto.CountryDto;
import com.hth.udecareer.model.response.CategoryDropdownResponse;
import com.hth.udecareer.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    @Operation(summary = "Dropdown: danh mục bài quiz")
    @GetMapping("/quiz/categories")
    public List<CategoryDropdownResponse> getQuizCategories() {
        return metadataService.getQuizCategoriesDropdown();
    }


    @Operation(summary = "Dropdown: danh mục bài viết (root categories)")
    @GetMapping("/post/categories")
    public List<CategoryDropdownResponse> getPostCategories() {
        return metadataService.getPostCategoriesDropdown();
    }

    @Operation(summary = "Dropdown: tất cả country", description = "Priority countries (VN, US, SG, etc.) appear first")
    @GetMapping("/countries")
    public List<CountryDto> getCountries(
            @RequestParam(required = false, defaultValue = "en") String locale
    ) {
        String[] isoCodes = Locale.getISOCountries();

        // Priority countries for business
        List<String> priorityCodes = Arrays.asList("VN", "US", "SG", "JP", "KR", "TH", "ID", "MY", "PH");

        Locale displayLocale = new Locale(locale);

        List<CountryDto> allCountries = Arrays.stream(isoCodes)
                .map(code -> new Locale("", code))
                .filter(countryLocale -> !countryLocale.getDisplayCountry(displayLocale).isEmpty())
                .map(countryLocale -> new CountryDto(
                    countryLocale.getCountry(),
                    countryLocale.getDisplayCountry(displayLocale)
                ))
                .sorted(Comparator.comparing(CountryDto::getName))
                .toList();

        // Separate priority and others
        List<CountryDto> priority = new ArrayList<>();
        List<CountryDto> others = new ArrayList<>();

        for (CountryDto country : allCountries) {
            if (priorityCodes.contains(country.getCode())) {
                priority.add(country);
            } else {
                others.add(country);
            }
        }

        // Sort priority by priority order
        priority.sort(Comparator.comparingInt(c -> priorityCodes.indexOf(c.getCode())));

        // Combine: priority first, then others
        List<CountryDto> result = new ArrayList<>(priority);
        result.addAll(others);

        return result;
    }
}

