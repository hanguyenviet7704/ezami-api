package com.hth.udecareer.utils;

import java.util.Objects;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BooleanUtil {

    public static boolean isTrue(Integer value) {
        return Objects.nonNull(value) && value > 0;
    }
}
