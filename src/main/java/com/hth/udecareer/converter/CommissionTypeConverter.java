package com.hth.udecareer.converter;

import com.hth.udecareer.enums.CommissionType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class CommissionTypeConverter implements AttributeConverter<CommissionType, String> {

    @Override
    public String convertToDatabaseColumn(CommissionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public CommissionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return CommissionType.fromValue(dbData);
    }
}
