package com.hth.udecareer.enums;

public enum LeaderBoardType {
    WEEK,
    MONTH,
    YEAR;

    public static LeaderBoardType fromString(String value) {
        if (value == null) return WEEK;
        switch (value.trim().toUpperCase()) {
            case "WEEK":  return WEEK;
            case "MONTH": return MONTH;
            case "YEAR":  return YEAR;
            default:      return WEEK;
        }
    }
}
