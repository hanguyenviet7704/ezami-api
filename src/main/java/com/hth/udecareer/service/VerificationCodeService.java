package com.hth.udecareer.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.service.Impl.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hth.udecareer.entities.VerificationCodeEntity;
import com.hth.udecareer.enums.VerificationCodeType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.VerificationCodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private static final long EXPIRE_TIME = 15 * 60;

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public void checkVerificationCode(@NotNull String email,
                                      @NotNull String verificationCode,
                                      @NotNull VerificationCodeType verificationType) throws AppException {
        final Optional<VerificationCodeEntity> verificationCodeEntityOpt =
                verificationCodeRepository.findByEmailAndCodeAndType(email,
                        verificationCode,
                        verificationType);

        final VerificationCodeEntity verificationCodeEntity = verificationCodeEntityOpt.orElseThrow(
                () -> new AppException(ErrorCode.INVALID_CONFIRMATION_CODE));

        if (isCodeExpired(verificationCodeEntity)) {
            throw new AppException(ErrorCode.EXPIRE_CONFIRMATION_CODE);
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public void createVerificationCode(@Nullable Long userId,
                                       @NotNull String email,
                                       @NotNull VerificationCodeType verificationType,
                                       boolean sendEmail) throws Exception {

        deleteOldVerificationCodes(email, verificationType);

        final String verificationCode = generateVerificationCode(email, verificationType);

        final VerificationCodeEntity verificationCodeEntity = new VerificationCodeEntity();
        verificationCodeEntity.setCode(verificationCode);
        verificationCodeEntity.setType(verificationType);
        verificationCodeEntity.setUserId(userId);
        verificationCodeEntity.setEmail(email);
        verificationCodeEntity.setExpiryTime(LocalDateTime.now().plusSeconds(EXPIRE_TIME));

        verificationCodeRepository.save(verificationCodeEntity);

        if (sendEmail) {
            if (Objects.requireNonNull(verificationType) == VerificationCodeType.RESET_PASS) {
                emailService.sendMail(email, "Reset password", getVerificationEmailContent(verificationCode, verificationType));
            } else if (Objects.requireNonNull(verificationType) == VerificationCodeType.REGISTER) {
                emailService.sendMail(email, "Register account", getVerificationEmailContent(verificationCode, verificationType));
            }
        }
    }

    private void deleteOldVerificationCodes(@NotNull String email, @NotNull VerificationCodeType verificationType) {
        final List<VerificationCodeEntity> oldCodes = verificationCodeRepository.findByEmailAndType(email, verificationType);
        verificationCodeRepository.deleteAll(oldCodes);
    }

    public void deleteVerificationCodeAfterUse(@NotNull String email,
                                               @NotNull String verificationCode,
                                               @NotNull VerificationCodeType verificationType) throws AppException {
        final Optional<VerificationCodeEntity> codeOpt =
                verificationCodeRepository.findByEmailAndCodeAndType(email, verificationCode, verificationType);

        if (codeOpt.isPresent()) {
            verificationCodeRepository.delete(codeOpt.get());
            log.info("Verification code deleted for email: {}", email);
        } else {
            log.warn("Attempted to delete non-existent verification code for email: {}", email);
        }
    }

    private String generateVerificationCode(@NotNull String email,
                                            @NotNull VerificationCodeType verificationType)
            throws Exception {
        final List<VerificationCodeEntity> entityList =
                verificationCodeRepository.findByEmailAndType(email, verificationType);
        final Set<String> codeList = entityList.stream()
                .filter(x -> !isCodeExpired(x))
                .map(VerificationCodeEntity::getCode)
                .collect(Collectors.toSet());

        for (int i = 0; i < 10; i++) {
            final String code = generateVerificationCode();
            if (!codeList.contains(code)) {
                return code;
            }
        }
        throw new Exception();
    }

    private static String generateVerificationCode() throws NoSuchAlgorithmException {
        return String.valueOf(SecureRandom.getInstanceStrong().nextLong(100000, 999999));
    }

    private static boolean isCodeExpired(@NotNull final VerificationCodeEntity verificationCodeEntity) {
        return verificationCodeEntity.getExpiryTime() != null
                && verificationCodeEntity.getExpiryTime().isBefore(LocalDateTime.now());
    }

    private static String getVerificationEmailContent(@NotNull String confirmationCode,
                                                      @NotNull VerificationCodeType verificationType) {
        String title = null;
        String message = null;
        if (Objects.requireNonNull(verificationType) == VerificationCodeType.RESET_PASS) {
            title = "Verification Code";
            message = "This is a one-time code that expires in 15 minutes. Use it to reset password. You can copy-paste it, there is no need to remember it.";
        } else if (Objects.requireNonNull(verificationType) == VerificationCodeType.REGISTER) {
            title = "Verification Code";
            message = "This is a one-time code that expires in 15 minutes. Use it to register account. You can copy-paste it, there is no need to remember it.";
        }
        return "<div style=\"background-color:#fff;margin:0;padding:0;width:100%\">\n" +
                "   <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\">\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\" style=\"background-color:#fff;padding-left:8px;padding-right:8px\">\n" +
                "               <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\" style=\"margin:0 auto;max-width:632px\">\n" +
                "                  <tbody>\n" +
                "                     <tr>\n" +
                "                        <td style=\"background-color:#fff;color:#fff;font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:28px;margin-bottom:0;margin-top:0;padding-bottom:10px;padding-left:24px;padding-right:24px;padding-top:10px\">\n" +
                "                           <h3 style=\"color:#000;font-family:Helvetica,Arial,sans-serif;font-size:24px;font-weight:700;line-height:31px;margin-bottom:16px;margin-top:0;text-align:center\">" + title + "</h3>\n" +
                "                           <p style=\"color:#010101;margin-bottom:0;margin-top:0;opacity:80%;text-align:left\">" + message + "</p>\n" +
                "                        </td>\n" +
                "                     </tr>\n" +
                "                  </tbody>\n" +
                "               </table>\n" +
                "            </td>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\">\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\" style=\"background-color:#fff;padding-left:8px;padding-right:8px\">\n" +
                "               <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\" style=\"margin:0 auto;max-width:632px\">\n" +
                "                  <tbody>\n" +
                "                     <tr>\n" +
                "                        <td align=\"center\" style=\"background-color:#fff;color:#82899a;font-family:Helvetica,Arial,sans-serif;font-size:14px;line-height:21px;margin-bottom:0;margin-top:0;padding-bottom:8px;padding-left:24px;padding-right:24px;padding-top:8px\">\n" +
                "                           <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                              <tbody>\n" +
                "                                 <tr>\n" +
                "                                    <td width=\"755\" align=\"center\" style=\"background-color:#e3e3e3;border-radius:10px;font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:28px;margin-bottom:0;margin-top:0;padding-bottom:10px;padding-left:8px;padding-right:8px;padding-top:10px\">\n" +
                "                                       <p style=\"border-radius:1000px;color:#242b3d;font-family:Arial;font-size:50px;font-weight:700;height:100px;line-height:100px;margin-bottom:0;margin-top:0;text-align:center;width:100%\"><strong>" + confirmationCode + "</strong></p>\n" +
                "                                    </td>\n" +
                "                                 </tr>\n" +
                "                              </tbody>\n" +
                "                           </table>\n" +
                "                        </td>\n" +
                "                     </tr>\n" +
                "                  </tbody>\n" +
                "               </table>\n" +
                "            </td>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "</div>";
    }
}
