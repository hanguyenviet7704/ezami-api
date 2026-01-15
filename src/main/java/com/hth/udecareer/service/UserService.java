package com.hth.udecareer.service;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.entities.FcomUserActivitiesEntity;
import com.hth.udecareer.entities.UserMetaEntity;
import com.hth.udecareer.enums.AffiliateStatus;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.LeaderBoardType;
import com.hth.udecareer.model.request.*;
import com.hth.udecareer.model.response.*;
import org.springframework.data.domain.Pageable;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.repository.FcomUserActivityRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.service.cache.CountryCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.VerificationCodeType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.security.PhpPasswordEncoder;
import com.hth.udecareer.model.dto.google.GoogleUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final String APP_CODE_DEFAULT = "ezami";
    private static final String KEY_CURRENT_POINTS = "current_points";
    private static final String KEY_POINT_WEEK        = "point_week";
    private static final String KEY_POINT_MONTH       = "point_month";
    private static final String KEY_POINT_YEAR        = "point_year";

    private static final String KEY_POINT_WEEK_START  = "point_week_start";
    private static final String KEY_POINT_MONTH_START = "point_month_start";
    private static final String KEY_POINT_YEAR_START  = "point_year_start";
    private static final String KEY_SETTING_ALLOW_PUSH = "setting_allow_push";

    private final UserRepository userRepository;
    private final PhpPasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final UserMetaRepository userMetaRepository;
    private final CountryCache countryCache;
    private final FcomUserActivityRepository fcomUserActivitiesRepository;
    private final UserPointsService userPointsService;
    private final AffiliateRepository affiliateRepository;
    private final com.hth.udecareer.eil.service.ReadinessService readinessService;

    public UserResponse findByEmail(String email) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        List<UserMetaEntity> metas = userMetaRepository.findByUserId(user.getId());

        String phone = null, country = null, dob = null, gender = null, url_image = null;
        for (UserMetaEntity meta : metas) {
            switch (meta.getMetaKey()) {
                case "url_image":
                    url_image = meta.getMetaValue();
                    break;
                case "phone":
                    phone = meta.getMetaValue();
                    break;
                case "country":
                    country = meta.getMetaValue();
                    break;
                case "dob":
                    dob = meta.getMetaValue();
                    break;
                case "gender":
                    gender = meta.getMetaValue();
                    break;
            }
        }

        UserFullResponse response = new UserFullResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNiceName(user.getNiceName());
        response.setDisplayName(user.getDisplayName());
        response.setUrl_image(url_image);
        response.setPhone(phone);
        response.setCountry(country);
        response.setDob(dob);
        response.setGender(gender); // Already set, good!
        return response;
    }

    public UserResponse signup(@NotNull RegisterWithCodeRequest request) throws Exception {
        final String appCode = StringUtils.isNotBlank(request.getAppCode()) ? request.getAppCode() : APP_CODE_DEFAULT;
        return signup(appCode,
                request.getEmail(),
                request.getPassword(),
                request.getVerificationCode(),
                true,
                request.getFullName(),
                request.getAffiliateId());
    }

    public UserResponse signup(@NotNull RegisterRequest request) throws Exception {
        final String appCode = StringUtils.isNotBlank(request.getAppCode()) ? request.getAppCode() : APP_CODE_DEFAULT;
        return signup(appCode,
                request.getEmail(),
                request.getPassword(),
                null,
                false,
                request.getFullName(),
                request.getAffiliateId());
    }

    private UserResponse signup(@NotNull String appCode,
                                @NotBlank String email,
                                @NotBlank String password,
                                @Nullable String code,
                                boolean verifyCode,
                                @NotBlank String fullName,
                                @Nullable Long affiliateId) throws AppException {


        if (verifyCode) {
            if (Objects.isNull(code)) {
                log.error("Verification code for user {} is null", email);
                throw new AppException(ErrorCode.INVALID_CODE);
            }
            try {
                verificationCodeService.checkVerificationCode(email, code, VerificationCodeType.REGISTER);

            } catch (Exception ex) {
                log.error(ex.getMessage() + ", email: {}, code: {}", email, code);
                throw ex;
            }
        }

        final Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            log.error("The email {} is already associated with an account.", email);
            throw new AppException(ErrorCode.EMAIL_INFO_EXISTED);
        }

        final String usernameFromEmail = getUsernameFromEmail(email);
        final Set<String> existedUsernames = userRepository.findByUsernameStartingWith(usernameFromEmail)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
        String username = null;
        int i = 1;
        do {
            final String tempUsername = usernameFromEmail + '_' + i;
            if (!existedUsernames.contains(tempUsername)) {
                username = tempUsername;
            }
            ++i;
        } while (Objects.isNull(username));

        final User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setUsername(username);
        user.setNiceName(username);
        user.setDisplayName(StringUtils.isNotBlank(fullName) ? fullName.trim() : username);
        user.setStatus(0);
        user.setActivationKey("");
        user.setUserUrl("");
        user.setRegisteredDate(LocalDateTime.now());
        user.setAppCode(appCode);

        final User savedUser = userRepository.save(user);

        if (affiliateId != null) {
            saveOrUpdateUserMeta(savedUser.getId(), "affiliate_id", String.valueOf(affiliateId));
            // Cộng điểm cho affiliate (người giới thiệu)
            awardAffiliatePoints(affiliateId, savedUser);
        }

        verificationCodeService.deleteVerificationCodeAfterUse(email, code, VerificationCodeType.REGISTER);
        return UserResponse.from(savedUser);
    }

    public void updateUserInfo(@NotNull UpdateUserInfoRequest request,
                               @NotNull String oldEmail) throws AppException {
        final User user = userRepository
                .findByEmail(oldEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        final String preferredName = StringUtils.isNotBlank(request.getDisplayName())
                ? request.getDisplayName().trim()
                : StringUtils.isNotBlank(request.getFullName()) ? request.getFullName().trim() : null;

        if (preferredName != null) {
            user.setDisplayName(preferredName);
        }

        if (StringUtils.isNotBlank(request.getNiceName())) {
            user.setNiceName(request.getNiceName().trim());
        }

        userRepository.save(user);

        // Cập nhật các thông tin meta vào UserMeta table
        saveOrUpdateUserMeta(user.getId(), "url_image", request.getUrlImage());
        saveOrUpdateUserMeta(user.getId(), "phone", request.getPhone());
        if (StringUtils.isNotBlank(request.getDob())) {
            try {
                LocalDate dob = LocalDate.parse(request.getDob(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                saveOrUpdateUserMeta(user.getId(), "dob", request.getDob());

            } catch (DateTimeException e) {
                throw new AppException(ErrorCode.INVALID_DOB_FORMAT);
            }
        }


        if (StringUtils.isNotBlank(request.getCountry()) && countryCache.isValidCountry(request.getCountry())) {
            saveOrUpdateUserMeta(user.getId(), "country", request.getCountry());
        }

        if (request.getGender() != null) {
            saveOrUpdateUserMeta(user.getId(), "gender", request.getGender().name());
        }
    }

    private void saveOrUpdateUserMeta(Long userId, String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        String newValue = value.trim();

        UserMetaEntity meta = userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .orElseGet(() -> {
                    UserMetaEntity newMeta = new UserMetaEntity();
                    newMeta.setUserId(userId);
                    newMeta.setMetaKey(key);
                    return newMeta;
                });

        if(newValue.equals(meta.getMetaValue())) {
            return;
        }

        meta.setMetaValue(value.trim());
        userMetaRepository.save(meta);
    }

    public void changePass(@NotNull ChangePassRequest request,
                           @NotNull String email) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPass(), user.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPass()));
        userRepository.save(user);
    }

    public void resetPass(@NotNull ResetPasswordRequest request) throws AppException {
        final User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        verificationCodeService.checkVerificationCode(request.getEmail(), request.getVerificationCode(),
                VerificationCodeType.RESET_PASS);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        verificationCodeService.deleteVerificationCodeAfterUse(request.getEmail(), request.getVerificationCode(),
                VerificationCodeType.RESET_PASS);
    }

    @Transactional
    public void deleteAccount(@NotNull String email, @Nullable DeleteAccountRequest request) throws AppException {
        if (request != null
                && StringUtils.isNotBlank(request.getConfirmationText())
                && !"DELETE".equals(request.getConfirmationText())) {
            throw new AppException(ErrorCode.INVALID_CONFIRMATION_TEXT);
        }
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        userRepository.delete(user);
    }

    public void generateVerificationCode(@NotNull String email,
                                         @NotNull VerificationCodeType type) throws Exception {
        final Optional<User> userOpt = userRepository.findByEmail(email);

        if (type == VerificationCodeType.RESET_PASS) {

            if (userOpt.isEmpty()) {
                throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
            }
            verificationCodeService.createVerificationCode(userOpt.get().getId(), email, type, true);

        } else if (type == VerificationCodeType.REGISTER) {

            if (userOpt.isPresent()) {
                throw new AppException(ErrorCode.EMAIL_INFO_EXISTED);
            }
            verificationCodeService.createVerificationCode(null, email, type, true);
        }
    }

    private static String getUsernameFromEmail(@NotNull final String email) {
        return Pattern.compile("([a-zA-Z]+)[^a-zA-Z@]*(@.*)?")
                .matcher(email).results()
                .map(result -> result.group(1))
                .collect(Collectors.joining(""));
    }

   
    public User findOrCreateGoogleUser(GoogleUserInfo userInfo) throws AppException {
        return findOrCreateGoogleUser(userInfo, null);
    }

 
    public User findOrCreateGoogleUser(GoogleUserInfo userInfo, Long affiliateId) throws AppException {
        final String email = userInfo.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new AppException(ErrorCode.GOOGLE_AUTH_EMAIL_MISSING);
        }

        final Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            log.info("Google Login: User {} đã tồn tại.", email);
            User existingUser = userOpt.get();
            
          
            return userRepository.save(existingUser);
        }

        log.info("Google Register: User {} chưa tồn tại. Đang tạo user mới với affiliateId: {}.", email, affiliateId);

        final String usernameFromEmail = getUsernameFromEmail(email);
        final Set<String> existedUsernames = userRepository.findByUsernameStartingWith(usernameFromEmail)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
        String username = null;
        int i = 1;
        do {
            final String tempUsername = usernameFromEmail + '_' + i;
            if (!existedUsernames.contains(tempUsername)) {
                username = tempUsername;
            }
            ++i;
        } while (Objects.isNull(username));

        final User user = new User();
        user.setEmail(email);

        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));

        user.setUsername(username);
        user.setNiceName(username);
        user.setDisplayName(userInfo.getName());
        user.setStatus(0);
        user.setActivationKey("");
        user.setUserUrl("");
        user.setRegisteredDate(LocalDateTime.now());
        user.setAppCode(APP_CODE_DEFAULT);

        final User savedUser = userRepository.save(user);

        
        if (affiliateId != null) {
            saveOrUpdateUserMeta(savedUser.getId(), "affiliate_id", String.valueOf(affiliateId));
            log.info("Saved affiliate_id {} for new Google user: {}", affiliateId, email);
            // Cộng điểm cho affiliate (người giới thiệu)
            awardAffiliatePoints(affiliateId, savedUser);
        }

        log.info("User registered successfully via Google: {}", email);

        return savedUser;
    }

    @Transactional
    public void updateNotificationSetting(String email, Boolean allowPush) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        saveOrUpdateUserMeta(user.getId(), KEY_SETTING_ALLOW_PUSH, String.valueOf(allowPush));
    }

    public boolean getNotificationSetting(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));
       return isAllowPush(user);
    }

    private boolean isAllowPush(User user) {
        UserMetaEntity userMeta = userMetaRepository.findByUserIdAndMetaKey(user.getId(), KEY_SETTING_ALLOW_PUSH).orElse(null);
        if(userMeta != null && userMeta.getMetaValue() != null && !userMeta.getMetaValue().isEmpty()) {
            return Boolean.parseBoolean(userMeta.getMetaValue());
        }
        return true;
    }



    public LeaderBoardWithUserResponse getLeaderboard(LeaderBoardType type, String email) {

        if(type == null) {
            type = LeaderBoardType.WEEK;
        }

        String pointKey;
        String startKey;
        String startValue;

        switch (type) {
            case WEEK:
                pointKey   = KEY_POINT_WEEK;
                startKey   = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
                break;
            case MONTH:
                pointKey   = KEY_POINT_MONTH;
                startKey   = KEY_POINT_MONTH_START;
                startValue = getMonthStartToday().toString();
                break;
            case YEAR:
                pointKey   = KEY_POINT_YEAR;
                startKey   = KEY_POINT_YEAR_START;
                startValue = getYearStartToday().toString();
                break;
            default:
                pointKey   = KEY_POINT_WEEK;
                startKey   = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
        }

        List<Object[]> rows = userMetaRepository.findTopUsersByKeyAndStart(pointKey, startKey, startValue);

        List<LeaderBoardResponse> topUsers = mapToLeaderboard(rows);

        UserRankDetail myRankDetail = null;
        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                String myScoreStr = userMetaRepository.findMyScoreByPeriod(user.getId(), pointKey, startKey, startValue);


                long myScore = (myScoreStr != null && !myScoreStr.isEmpty()) ? Long.parseLong(myScoreStr) : 0L;


                long countHigher = userMetaRepository.countUsersHigherPeriod(pointKey, myScore, startKey, startValue);
                int myRank = 0;
                if(myScore > 0) {
                    myRank = (int) (countHigher + 1);
                }

                Long nextHigherScore = userMetaRepository.findNextHigherScorePeriod(pointKey, myScore, startKey, startValue);
                long pointsToNext = (nextHigherScore != null) ? (nextHigherScore - myScore + 1) : 0;

                String avatar = getMeta(user.getId(), "url_image");

                myRankDetail = UserRankDetail.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .avatarUrl(avatar)
                        .totalPoints(myScore)
                        .rank(myRank)
                        .pointsToNextRank(pointsToNext)
                        .build();
            }
        }

        String currentTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return LeaderBoardWithUserResponse.builder()
                .topUsers(topUsers)
                .myRank(myRankDetail)
                .updateTime(currentTimeStr)
                .build();
    }

    public LeaderBoardWithUserPageResponse getLeaderboardWithPagination(LeaderBoardType type, String email, int page, int size) {
        if(type == null) {
            type = LeaderBoardType.WEEK;
        }

        String pointKey;
        String startKey;
        String startValue;

        switch (type) {
            case WEEK:
                pointKey   = KEY_POINT_WEEK;
                startKey   = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
                break;
            case MONTH:
                pointKey   = KEY_POINT_MONTH;
                startKey   = KEY_POINT_MONTH_START;
                startValue = getMonthStartToday().toString();
                break;
            case YEAR:
                pointKey   = KEY_POINT_YEAR;
                startKey   = KEY_POINT_YEAR_START;
                startValue = getYearStartToday().toString();
                break;
            default:
                pointKey   = KEY_POINT_WEEK;
                startKey   = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
        }

        // Validate pagination
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        int offset = page * size;
        List<Object[]> rows = userMetaRepository.findUsersByKeyAndStartWithPagination(pointKey, startKey, startValue, size, offset);
        long total = userMetaRepository.countUsersByKeyAndStart(pointKey, startKey, startValue);

        // Calculate rank for each user (rank = offset + index + 1)
        List<LeaderBoardResponse> topUsers = new ArrayList<>();
        int rank = offset + 1;
        for (Object[] row : rows) {
            Long userId = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            String username = row[1] != null ? (String) row[1] : "";
            String name = row[2] != null ? (String) row[2] : "Unknown User";
            Long points = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            String avatar = row[4] != null ? (String) row[4] : "";

            topUsers.add(LeaderBoardResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(username)
                    .displayName(name)
                    .totalPoints(points)
                    .avatarUrl(avatar)
                    .build());
        }

        UserRankDetail myRankDetail = null;
        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                String myScoreStr = userMetaRepository.findMyScoreByPeriod(user.getId(), pointKey, startKey, startValue);

                long myScore = (myScoreStr != null && !myScoreStr.isEmpty()) ? Long.parseLong(myScoreStr) : 0L;

                long countHigher = userMetaRepository.countUsersHigherPeriod(pointKey, myScore, startKey, startValue);
                int myRank = 0;
                if(myScore > 0) {
                    myRank = (int) (countHigher + 1);
                }

                Long nextHigherScore = userMetaRepository.findNextHigherScorePeriod(pointKey, myScore, startKey, startValue);
                long pointsToNext = (nextHigherScore != null) ? (nextHigherScore - myScore + 1) : 0;

                String avatar = getMeta(user.getId(), "url_image");

                myRankDetail = UserRankDetail.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .avatarUrl(avatar)
                        .totalPoints(myScore)
                        .rank(myRank)
                        .pointsToNextRank(pointsToNext)
                        .build();
            }
        }

        String currentTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Create PageResponse
        PageResponse<LeaderBoardResponse> pageResponse = PageResponse.of(topUsers, 
                org.springframework.data.domain.PageRequest.of(page, size), total);

        return LeaderBoardWithUserPageResponse.builder()
                .topUsers(pageResponse)
                .myRank(myRankDetail)
                .updateTime(currentTimeStr)
                .build();
    }


    public List<RankBenefitResponse> getRankBenefits() {
        List<RankBenefitResponse> benefits = new ArrayList<>();

        // Top 1
        benefits.add(RankBenefitResponse.builder()
                .tierName("Quán Quân")
                .range("1")
                .description("Huy hiệu Quán Quân vĩnh viễn, Avatar khung vàng, +1000 điểm uy tín.")
                .iconUrl("https://your-domain.com/icons/rank_1_gold.png")
                .build());


        benefits.add(RankBenefitResponse.builder()
                .tierName("Á Quân")
                .range("2-3")
                .description("Huy hiệu Bạc, Avatar khung bạc, +500 điểm uy tín.")
                .iconUrl("https://your-domain.com/icons/rank_2_3_silver.png")
                .build());

        benefits.add(RankBenefitResponse.builder()
                .tierName("Top 10 Xuất Sắc")
                .range("4-10")
                .description("Huy hiệu Đồng, +200 điểm uy tín.")
                .iconUrl("https://your-domain.com/icons/rank_4_10_bronze.png")
                .build());

        return benefits;
    }

    private List<LeaderBoardResponse> mapToLeaderboard(List<Object[]> rows) {
        List<LeaderBoardResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Long userId = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            String username = row[1] != null ? (String) row[1] : "";
            String name = row[2] != null ? (String) row[2] : "Unknown User";
            Long points = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            String avatar = row[4] != null ? (String) row[4] : "";

            leaderboard.add(LeaderBoardResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(username)
                    .displayName(name)
                    .totalPoints(points)
                    .avatarUrl(avatar)
                    .build());
        }
        return leaderboard;
    }


    private LocalDate getWeekStartToday() {
        LocalDate today = LocalDate.now();
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate getMonthStartToday() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(1);
    }

    private LocalDate getYearStartToday() {
        LocalDate today = LocalDate.now();
        return today.withDayOfYear(1);
    }


    private String getMeta(Long userId, String key) {
        return userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .map(UserMetaEntity::getMetaValue)
                .orElse(null);
    }

    private void setMeta(Long userId, String key, String value) {
        UserMetaEntity meta = userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .orElseGet(() -> {
                    UserMetaEntity m = new UserMetaEntity();
                    m.setUserId(userId);
                    m.setMetaKey(key);
                    return m;
                });
        meta.setMetaValue(value);
        userMetaRepository.save(meta);
    }


    private void awardAffiliatePoints(Long affiliateId, User savedUser) {
        try {
            Affiliate affiliate = affiliateRepository.findById(affiliateId)
                    .orElse(null);

            if (affiliate == null) {
                log.warn("Affiliate with id {} not found. Skipping points award.", affiliateId);
                return;
            }

            // Lấy user của affiliate để cộng điểm
            User affiliateUser = userRepository.findById(affiliate.getUserId())
                    .orElse(null);

            if (affiliateUser == null) {
                log.warn("User with id {} (affiliate user) not found. Skipping points award.", affiliate.getUserId());
                return;
    }

            // Cộng điểm cho affiliate (người giới thiệu)
            // Sử dụng relatedId là savedUser.getId() để tránh cộng điểm trùng lặp
            try {
                userPointsService.addPoints(
                    affiliateUser.getEmail(),
                    100,
                    "POINT_AFFILIATE_GIVE",
                    null,
                    savedUser.getId()
                );
                log.info("Awarded 100 points to affiliate {} (user: {}) for new user registration (userId: {})",
                    affiliateId, affiliateUser.getEmail(), savedUser.getId());
            } catch (AppException e) {
                log.info("Affiliate points already awarded for affiliate {} and user {}. Skipping.",
                    affiliateId, savedUser.getId());
        }
        } catch (Exception e) {
            log.error("Error awarding affiliate points for affiliateId {} and userId {}: {}",
                affiliateId, savedUser.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    public UserResponse completeOnboarding(@NotNull String email, @Nullable OnboardingCompleteRequest request) throws AppException {
        log.info("Completing onboarding for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        // Save onboarding_completed metadata
        saveUserMeta(user.getId(), "onboarding_completed", "true");

        // Save optional onboarding data if provided
        if (request != null) {
            if (StringUtils.isNotBlank(request.getSelectedCareerPath())) {
                saveUserMeta(user.getId(), "onboarding_career_path", request.getSelectedCareerPath());
            }

            if (request.getSelectedCertifications() != null && !request.getSelectedCertifications().isEmpty()) {
                saveUserMeta(user.getId(), "onboarding_certifications",
                        String.join(",", request.getSelectedCertifications()));
            }

            if (StringUtils.isNotBlank(request.getTargetDate())) {
                saveUserMeta(user.getId(), "onboarding_target_date", request.getTargetDate());
            }

            if (StringUtils.isNotBlank(request.getExperienceLevel())) {
                saveUserMeta(user.getId(), "onboarding_experience_level", request.getExperienceLevel());
            }

            if (request.getWeeklyStudyHours() != null) {
                saveUserMeta(user.getId(), "onboarding_weekly_study_hours",
                        String.valueOf(request.getWeeklyStudyHours()));
            }
        }

        log.info("Onboarding completed successfully for user: {}", email);
        return findByEmail(email);
    }

    private void saveUserMeta(Long userId, String metaKey, String metaValue) {
        // Check if meta already exists
        Optional<UserMetaEntity> existingMeta = userMetaRepository.findByUserIdAndMetaKey(userId, metaKey);

        if (existingMeta.isPresent()) {
            // Update existing
            UserMetaEntity meta = existingMeta.get();
            meta.setMetaValue(metaValue);
            userMetaRepository.save(meta);
        } else {
            // Create new
            UserMetaEntity meta = new UserMetaEntity();
            meta.setUserId(userId);
            meta.setMetaKey(metaKey);
            meta.setMetaValue(metaValue);
            userMetaRepository.save(meta);
        }
    }

    /**
     * Get mock test readiness score for user
     * This is a convenience wrapper around ReadinessService.getMyLatest()
     */
    public com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity getMockTestReadiness(
            java.security.Principal principal, String testType) {
        return readinessService.getMyLatest(principal, testType);
    }

}
