package com.hth.udecareer.enums;

import lombok.Getter;

@Getter
public enum SupportChannel {

    EMAIL("email", "support@ezami.io"),
    ZALO("zaloOA", "1234567890"),
    MESSENGER("messengerPageId", "987654321"),
    AUTO("autoReplyEnabled", "true");

    private final String channel;
    private final String contact;

    SupportChannel(String channel, String contact) {
        this.channel = channel;
        this.contact = contact;
    }
}
