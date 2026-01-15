package com.hth.udecareer.enums;

public enum DevicePlatform {
    ANDROID("Android"),
    IOS("iOS"),
    WEB("Web");

    private final String displayName;

    DevicePlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

