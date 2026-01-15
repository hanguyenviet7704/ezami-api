package com.hth.udecareer.repository;


import com.hth.udecareer.entities.Voucher;
import com.hth.udecareer.model.response.VoucherResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, String> {

    @Query("""
            SELECT new com.hth.udecareer.model.response.VoucherResponse(
                v.voucherId, v.title, v.code, v.discountType,
                v.discountValue, v.validFrom, v.validTo, uv.status
            )
            FROM UserVoucher uv
            JOIN Voucher v ON uv.voucherId = v.voucherId
            WHERE uv.userId = :userId
            ORDER BY uv.receivedAt DESC
            """)
    List<VoucherResponse> findVoucherListByUserId(@Param("userId") Long userId);

    Voucher findByVoucherId(String voucherId);

    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.status = 'active' " +
           "AND v.validFrom <= CURRENT_DATE AND v.validTo >= CURRENT_DATE")
    java.util.Optional<Voucher> findValidVoucherByCode(@Param("code") String code);

    java.util.Optional<Voucher> findByCode(String code);
}
