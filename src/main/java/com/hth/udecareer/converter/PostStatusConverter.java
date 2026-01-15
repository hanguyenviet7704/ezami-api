package com.hth.udecareer.converter;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.hth.udecareer.enums.PostStatus;

@Converter
public class PostStatusConverter implements AttributeConverter<PostStatus, String> {
    @Nullable
    @Override
    public String convertToDatabaseColumn(PostStatus type) {
        return type != null ? type.getValue() : null;
    }

    @Nullable
    @Override
    public PostStatus convertToEntityAttribute(String value) {
        return value != null ? PostStatus.getByValue(value) : null;
    }
}
