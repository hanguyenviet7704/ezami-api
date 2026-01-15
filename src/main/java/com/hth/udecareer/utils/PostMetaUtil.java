package com.hth.udecareer.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class PostMetaUtil {

    private static final Pattern S_PATTERN = Pattern.compile("s:\\d*:\"([\\S\\s]*)\"");
    private static final Pattern META_VALUE_PATTERN = Pattern.compile(
            "(s:\\d+:\"[\\S\\s]*?\";)|(b:[0,1];)|(i:\\d+;)|(N;)");

    // utility class (no main method)

    public static Map<String, Object> getPostMetaValuesNew(String metaValue) {
        final Matcher matcher = META_VALUE_PATTERN.matcher(metaValue);
        final Map<String, Object> metaMap = new HashMap<>();
        final List<String> valueList = new ArrayList<>();

        while (matcher.find()) {
            valueList.add(StringUtils.trimTrailingCharacter(matcher.group(0), ';'));
        }

        for (int i = 0; i < valueList.size() - 1; i += 2) {
            final Pair<String, Object> pair = getSettingValue(valueList.get(i), valueList.get(i + 1));
            if (Objects.nonNull(pair)) {
                metaMap.put(pair.getFirst(), pair.getSecond());
            }
        }
        return metaMap;
    }

    public static Map<String, Object> getPostMetaValues(String metaValue) {
        final String[] arr = metaValue.split(";");
        final Map<String, Object> metaMap = new HashMap<>();

        for (int i = 0; i < arr.length - 1; i += 2) {
            final Pair<String, Object> pair = getSettingValue(arr[i], arr[i + 1]);
            if (Objects.nonNull(pair)) {
                metaMap.put(pair.getFirst(), pair.getSecond());
            }
        }

        return metaMap;
    }

    @Nullable
    private static Pair<String, Object> getSettingValue(String keyStr, String valueStr) {
        final Object key = getValue(keyStr);
        final Object value = getValue(valueStr);

        if (Objects.nonNull(key)
                && key instanceof String
                && Objects.nonNull(value)) {
            return Pair.of(key.toString(), value);
        }

        return null;
    }

    @Nullable
    private static Object getValue(String valueStr) {
        if (valueStr.startsWith("s")) {
            final Matcher matcher = S_PATTERN.matcher(valueStr);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        final String[] arr = valueStr.split(":");
        if (valueStr.startsWith("i")) {
            return Integer.valueOf(arr[1]);
        }
        if (valueStr.startsWith("b")) {
            return BooleanUtil.isTrue(Integer.valueOf(arr[1]));
        }
        return null;
    }
}
