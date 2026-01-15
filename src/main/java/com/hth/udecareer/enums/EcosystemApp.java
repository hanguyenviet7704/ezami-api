package com.hth.udecareer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EcosystemApp {

    MAZII("Mazii - Từ Điển Tiếng Nhật",
            "Người bạn đồng hành đáng tin cậy trong việc học tiếng Nhật",
            "https://mazii.net/assets/images/logo/logo_web_white.png",
            "https://apps.apple.com/us/app/mazii-dict-to-learn-japanese/id933081417",
            "https://play.google.com/store/apps/details?id=com.mazii.dictionary"),

    HANZII("Hanzii - Từ Điển Tiếng Trung",
            "Hỗ trợ học tiếng Trung Quốc hiệu quả",
            "https://hanzii.net/assets/images/logo.png",
            "https://apps.apple.com/vn/app/t%E1%BB%AB-%C4%91i%E1%BB%83n-trung-vi%E1%BB%87t-hanzii/id1468400944?l=vi",
            "https://play.google.com/store/apps/details?id=com.eup.hanzii"),

    JAEMY("Jaemy - Từ Điển Tiếng Hàn",
            "Hỗ trợ học tiếng Hàn với ngữ cảnh văn hóa chính thống",
            "https://jaemy.net/assets/images/ic_logo.png",
            "https://apps.apple.com/vn/app/t%E1%BB%AB-%C4%91i%E1%BB%83n-h%C3%A0n-vi%E1%BB%87t-jaemy/id1614352915?l=vi",
            "https://play.google.com/store/apps/details?id=com.jaemy.koreandictionary"),

    DUNNO("Dunno - Từ Điển Tiếng Anh",
            "Từ điển Anh-Việt chuyên nghiệp cho học tập và làm việc",
            "https://dunno.ai/assets/images/logo.png",
            "https://apps.apple.com/vn/app/english-dictionary-dunno/id1564720498",
            "https://play.google.com/store/apps/details?id=ai.dunno.dict"),

    FAZTAA("Faztaa - Từ Điển Tiếng Đức",
            "Từ điển Đức-Việt chuẩn quốc tế",
            "https://faztaa.com/assets/images/logo.ico",
            "https://apps.apple.com/vn/app/faztaa-german-dictionary/id6443765652",
            "https://play.google.com/store/apps/details?id=com.eup.faztaa");

    private final String name;
    private final String description;
    private final String logoUrl;
    private final String appStoreUrl;
    private final String googlePlayUrl;
}
