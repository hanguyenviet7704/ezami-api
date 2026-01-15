package com.hth.udecareer.service.cache;

import com.hth.udecareer.model.dto.CountryDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CountryCache {
    private  final List<CountryDto> countryList;

    public CountryCache() {
        String[] isoCodes = Locale.getISOCountries();

        countryList = Arrays.stream(isoCodes)
                .map(code -> new Locale("", code))
                .filter(locale -> !locale.getDisplayCountry().isEmpty())
                .map(locale -> new CountryDto(locale.getCountry(), locale.getDisplayCountry()))
                .sorted(Comparator.comparing(CountryDto::getName))
                .collect(Collectors.toList());
    }

    public List<CountryDto> getCountryList() {
        return countryList;
    }

    public boolean isValidCountry(String code) {
        return countryList.stream().anyMatch(country -> country.getName().equals(code));
    }
}
