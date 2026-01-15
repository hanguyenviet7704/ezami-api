package com.hth.udecareer.utils;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamUtil {

    public static <T> List<T> getNullableListSafely(List<T> source) {
        return ofNullable(source).orElse(emptyList());
    }

    public static <T> Stream<T> safeStream(List<T> source) {
        return getNullableListSafely(source).stream();
    }
}
