package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VnpayIpnResponse {
    private String RspCode;
    private String Message;
}
