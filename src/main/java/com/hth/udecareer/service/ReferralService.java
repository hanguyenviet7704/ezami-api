package com.hth.udecareer.service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserMetaEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ApplyReferralRequest;
import com.hth.udecareer.model.response.ReferralCodeResponse;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private static final String KEY_REF_CODE = "referral_code";
    private static final String KEY_REF_COUNT = "referral_count";
    private static final String KEY_REFERRED_BY = "referred_by";

    private static final String ALPHABET = "Q8W4E3R2T5Y1U0I9OPLKJHG7FDSA6ZXCVBNM";
    private static final int BASE = ALPHABET.length();
    private static final long MODULO = 78364164096L;
    private static final long MULTIPLIER = 56000000029L;
    private static final long INCREMENT = 100000000L;

    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;
    private final UserPointsService userPointsService;

    @Transactional
    public ReferralCodeResponse createReferralCode(String email) {
        User user = userRepository.findByEmailWithLock(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        UserMetaEntity existingCode = userMetaRepository
                .findByUserIdAndMetaKey(user.getId(), KEY_REF_CODE)
                .orElse(null);

        UserMetaEntity referredByMeta = userMetaRepository
                .findByUserIdAndMetaKey(user.getId(), KEY_REFERRED_BY)
                .orElse(null);

        boolean isReferral = referredByMeta != null
                && referredByMeta.getMetaValue() != null
                && !referredByMeta.getMetaValue().trim().isEmpty();

        ReferralCodeResponse response = new ReferralCodeResponse();

        if (existingCode != null && existingCode.getMetaValue() != null && !existingCode.getMetaValue().isEmpty()) {
            response.setReferralCode(existingCode.getMetaValue());
            response.setIsReferral(isReferral);
            return response;
        }

        String newCode = generateCode(user.getId());

        UserMetaEntity codeMeta = new UserMetaEntity();
        codeMeta.setUserId(user.getId());
        codeMeta.setMetaKey(KEY_REF_CODE);
        codeMeta.setMetaValue(newCode);
        userMetaRepository.save(codeMeta);

        UserMetaEntity countMeta = new UserMetaEntity();
        countMeta.setUserId(user.getId());
        countMeta.setMetaKey(KEY_REF_COUNT);
        countMeta.setMetaValue("0");
        userMetaRepository.save(countMeta);

        response.setReferralCode(newCode);
        response.setIsReferral(isReferral);
        return response;
    }

    @Transactional
    public void applyReferralCode(ApplyReferralRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        UserMetaEntity existingReferral = userMetaRepository
                .findByUserIdAndMetaKey(user.getId(), KEY_REFERRED_BY)
                .orElse(null);

        if (existingReferral != null
                && existingReferral.getMetaValue() != null
                && !existingReferral.getMetaValue().isEmpty()) {
            throw new AppException(ErrorCode.REFERRAL_ALREADY_APPLIED);
        }

        UserMetaEntity referralMeta = userMetaRepository
                .findByMetaKeyAndMetaValue(KEY_REF_CODE, request.getReferralCode())
                .orElseThrow(() -> new AppException(ErrorCode.REFERRAL_CODE_NOT_FOUND));

        Long referrerId = referralMeta.getUserId();

        User referrer = userRepository.findById(referrerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (referrerId.equals(user.getId())) {
            throw new AppException(ErrorCode.SELF_REFERRAL_NOT_ALLOWED);
        }

        if (referrer.getRegisteredDate().isAfter(user.getRegisteredDate())) {
            throw new AppException(ErrorCode.INVALID_REFERRAL_CODE);
        }

        UserMetaEntity referredByMeta = new UserMetaEntity();
        referredByMeta.setUserId(user.getId());
        referredByMeta.setMetaKey(KEY_REFERRED_BY);
        referredByMeta.setMetaValue(String.valueOf(referrerId));
        userMetaRepository.save(referredByMeta);

        incrementReferralCount(referrerId);

        try {
            userPointsService.addPoints(referrer.getEmail(), 50, "POINT_REFERRAL_GIVE", null, user.getId());
            userPointsService.addPoints(user.getEmail(), 10, "POINT_REFERRAL_RECEIVE", null, referrerId);
        } catch (AppException e) {
            log.info("Referral points already awarded. Skipping.");
        }
    }

    private String generateCode(long userId) {
        long target = ((userId + INCREMENT) * MULTIPLIER) % MODULO;

        StringBuilder result = new StringBuilder();
        do {
            int remainder = (int) (target % BASE);
            result.append(ALPHABET.charAt(remainder));
            target /= BASE;
        } while (target > 0);

        while (result.length() < 7) {
            result.append(ALPHABET.charAt(0));
        }

        return result.reverse().toString();
    }

    private void incrementReferralCount(Long referrerId) {
        int updatedRows = userMetaRepository.incrementMetaValue(referrerId, KEY_REF_COUNT);

        if (updatedRows == 0) {
            UserMetaEntity countMeta = new UserMetaEntity();
            countMeta.setUserId(referrerId);
            countMeta.setMetaKey(KEY_REF_COUNT);
            countMeta.setMetaValue("1");

            try {
                userMetaRepository.save(countMeta);
            } catch (Exception e) {
                userMetaRepository.incrementMetaValue(referrerId, KEY_REF_COUNT);
            }
        }
    }
}
