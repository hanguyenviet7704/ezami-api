package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.Voucher;
import com.hth.udecareer.model.response.VoucherResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper {

    VoucherResponse toVoucherResponse (Voucher voucher);

}
