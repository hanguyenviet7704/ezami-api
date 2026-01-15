package com.hth.udecareer.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EcosystemAppResponse {
    private String code;
    private String name;
    private String description;
    private String logoUrl;
    private String appStoreUrl;
    private String googlePlayUrl;
}
