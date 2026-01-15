package com.hth.udecareer.service;

import com.hth.udecareer.entities.Voucher;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.VoucherRepository;
import com.hth.udecareer.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Service for voucher validation and discount calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherValidationService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    /**
     * Validate and calculate discount for a voucher code.
     *
     * @param voucherCode The voucher code to validate
     * @param userId The user ID applying the voucher
     * @param originalAmount The original amount before discount
     * @return VoucherDiscount containing discount details
     * @throws AppException if voucher is invalid
     */
    public VoucherDiscount validateAndCalculateDiscount(String voucherCode, Long userId, BigDecimal originalAmount) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return VoucherDiscount.none();
        }

        String code = voucherCode.trim().toUpperCase();

        // Find voucher by code
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND, "Voucher not found: " + code));

        // Check if voucher is active
        if (!"active".equalsIgnoreCase(voucher.getStatus())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED, "Voucher is not active");
        }

        // Check validity period
        LocalDate today = LocalDate.now();
        if (today.isBefore(voucher.getValidFrom())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_YET_VALID,
                    "Voucher is not yet valid. Valid from: " + voucher.getValidFrom());
        }
        if (today.isAfter(voucher.getValidTo())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED,
                    "Voucher has expired on: " + voucher.getValidTo());
        }

        // Check if user already used this voucher
        boolean alreadyUsed = userVoucherRepository.existsByUserIdAndVoucherIdAndStatus(
                userId, voucher.getVoucherId(), "USED");
        if (alreadyUsed) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_USED, "You have already used this voucher");
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(voucher, originalAmount);
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        // Ensure final amount is not negative
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
            discountAmount = originalAmount;
        }

        log.info("Voucher {} applied. Original: {}, Discount: {}, Final: {}",
                code, originalAmount, discountAmount, finalAmount);

        return VoucherDiscount.builder()
                .voucherId(voucher.getVoucherId())
                .voucherCode(code)
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal originalAmount) {
        String discountType = voucher.getDiscountType().toLowerCase();
        BigDecimal discountValue = voucher.getDiscountValue();

        if ("percentage".equals(discountType) || "percent".equals(discountType)) {
            // Percentage discount (e.g., 10% off)
            return originalAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else if ("fixed".equals(discountType) || "amount".equals(discountType)) {
            // Fixed amount discount
            return discountValue;
        } else {
            log.warn("Unknown discount type: {}. Treating as fixed amount.", discountType);
            return discountValue;
        }
    }

    /**
     * Mark voucher as used after successful payment.
     */
    public void markVoucherAsUsed(Long userId, String voucherId) {
        userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId)
                .ifPresent(userVoucher -> {
                    userVoucher.setStatus("USED");
                    userVoucherRepository.save(userVoucher);
                    log.info("Marked voucher {} as USED for user {}", voucherId, userId);
                });
    }

    /**
     * DTO for voucher discount calculation result.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VoucherDiscount {
        private String voucherId;
        private String voucherCode;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal originalAmount;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;

        public static VoucherDiscount none() {
            return VoucherDiscount.builder()
                    .discountAmount(BigDecimal.ZERO)
                    .build();
        }

        public boolean hasDiscount() {
            return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
        }
    }
}
