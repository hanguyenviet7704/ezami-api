package com.hth.udecareer.converter;

import com.hth.udecareer.enums.DeviceType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DeviceTypeConverter implements AttributeConverter<DeviceType, String> {

    @Override
    public String convertToDatabaseColumn(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }
        return deviceType.name().toLowerCase();
    }

    @Override
    public DeviceType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return DeviceType.UNKNOWN;
        }

        try {
            return DeviceType.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown DeviceType value from database: " + dbData + ". Using UNKNOWN as default.");
            return DeviceType.UNKNOWN;
        }
    }
}

