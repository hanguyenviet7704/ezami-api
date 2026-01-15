package com.hth.udecareer.model.response;


import com.hth.udecareer.model.dto.DeviceInfo;
import com.hth.udecareer.model.dto.SenderInfo;
import com.hth.udecareer.repository.SupportLogRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SupportAdminResponse extends SupportResponse {

    private SenderInfo sender;
    private DeviceInfo deviceInfo;

}
