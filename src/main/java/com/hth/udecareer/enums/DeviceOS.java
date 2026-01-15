package com.hth.udecareer.enums;

import lombok.Getter;

@Getter
public enum DeviceOS {
    WINDOWS("windows"),
    MACOS("mac"),
    LINUX("linux"),
    IOS("ios"),
    ANDROID("android"),
    UNKNOWN("unknown");

    private final String value;

    DeviceOS(String value) {
        this.value = value;
    }

    public static DeviceOS fromString(String os) {
        if (os == null) return UNKNOWN;
        String normalized = os.trim().toLowerCase();
        switch (normalized) {
            case "windows": case "win": return WINDOWS;
            case "macos": case "mac": return MACOS;
            case "linux": return LINUX;
            case "ios": return IOS;
            case "android": return ANDROID;
            default: return UNKNOWN;
        }
    }
}
