package com.hth.udecareer.converter;

import com.hth.udecareer.enums.CommissionStatus;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class CommissionStatusConverter implements AttributeConverter<CommissionStatus, String> {

    @Override
    public String convertToDatabaseColumn(CommissionStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public CommissionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return CommissionStatus.fromValue(dbData);
    }
}

