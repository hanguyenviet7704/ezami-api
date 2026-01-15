package com.hth.udecareer.converter;

import com.hth.udecareer.enums.LinkType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LinkTypeConverter implements AttributeConverter<LinkType, String> {

    @Override
    public String convertToDatabaseColumn(LinkType linkType) {
        if (linkType == null) {
            return null;
        }
        return linkType.name().toLowerCase();
    }

    @Override
    public LinkType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            return LinkType.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown LinkType value from database: " + dbData + ". Using CUSTOM as default.");
            return LinkType.CUSTOM;
        }
    }
}
