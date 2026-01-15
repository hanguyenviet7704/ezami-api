package com.hth.udecareer.converter;

import com.hth.udecareer.enums.RuleType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RuleTypeConverter implements AttributeConverter<RuleType, String> {

    @Override
    public String convertToDatabaseColumn(RuleType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public RuleType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return RuleType.fromValue(dbData);
    }
}

