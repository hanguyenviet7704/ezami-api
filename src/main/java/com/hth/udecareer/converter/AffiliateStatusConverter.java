package com.hth.udecareer.converter;

import com.hth.udecareer.enums.AffiliateStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AffiliateStatusConverter implements AttributeConverter<AffiliateStatus, String> {

    @Override
    public String convertToDatabaseColumn(AffiliateStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public AffiliateStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // accept lowercase/ mixed case values from DB
        return AffiliateStatus.valueOf(dbData.trim().toUpperCase());
    }
}

