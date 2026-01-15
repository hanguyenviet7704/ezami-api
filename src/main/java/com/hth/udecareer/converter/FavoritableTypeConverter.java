package com.hth.udecareer.converter;

import com.hth.udecareer.enums.FavoritableType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class FavoritableTypeConverter implements AttributeConverter<FavoritableType, String> {

    @Override
    public String convertToDatabaseColumn(FavoritableType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue(); // Lưu giá trị lowercase vào DB
    }

    @Override
    public FavoritableType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FavoritableType.fromString(dbData); // Convert từ lowercase sang enum
    }
}
