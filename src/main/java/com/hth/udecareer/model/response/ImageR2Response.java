package com.hth.udecareer.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageR2Response {
    private String fileKey;
    private String url;
}
