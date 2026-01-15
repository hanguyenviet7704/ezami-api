package com.hth.udecareer.converter;

import com.hth.udecareer.enums.FavoriteStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class FavoriteStatusConverter implements AttributeConverter<FavoriteStatus, String> {

    @Override
    public String convertToDatabaseColumn(FavoriteStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue(); // Lưu giá trị lowercase vào DB
    }

    @Override
    public FavoriteStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FavoriteStatus.fromString(dbData); // Convert từ lowercase sang enum
    }
}
