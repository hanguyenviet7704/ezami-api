package com.hth.udecareer.service;

import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.PurchaseFilterRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.PurchaseHistoryResponse;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.specification.PurchaseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    private final UserPurchasedRepository userPurchasedRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<PurchaseHistoryResponse> searchAndFilterPurchases(
            @NotNull PurchaseFilterRequest filter,
            @NotNull Pageable pageable) throws AppException {

        log.debug("Searching purchases with filter: {} and pagination: {}", filter, pageable);

        filter.validate();

        Specification<UserPurchasedEntity> spec = PurchaseSpecification.buildSpecification(filter);
        Page<UserPurchasedEntity> purchasePage = userPurchasedRepository.findAll(spec, pageable);

        List<PurchaseHistoryResponse> responses = purchasePage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PurchaseHistoryResponse>builder()
                .content(responses)
                .page(purchasePage.getNumber())
                .size(purchasePage.getSize())
                .totalElements(purchasePage.getTotalElements())
                .totalPages(purchasePage.getTotalPages())
                .hasNext(purchasePage.hasNext())
                .hasPrevious(purchasePage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PurchaseHistoryResponse> searchAndFilterPurchasesAsList(
            @NotNull PurchaseFilterRequest filter) throws AppException {
        
        log.debug("Searching purchases without pagination - Filter: {}", filter);
        
        filter.validate();

        // For simple userId-only queries, use optimized method with JOIN FETCH
        if (isSimpleUserIdQuery(filter)) {
            List<UserPurchasedEntity> purchases = userPurchasedRepository.findAllByUserId(filter.getUserId());
            log.info("Found {} purchases using optimized query", purchases.size());
            
            return purchases.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }

        // For complex queries, use Specification
        Specification<UserPurchasedEntity> spec = PurchaseSpecification.buildSpecification(filter);
        List<UserPurchasedEntity> purchaseList = userPurchasedRepository.findAll(spec);

        return purchaseList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if the filter request is a simple userId-only query
     */
    private boolean isSimpleUserIdQuery(PurchaseFilterRequest filter) {
        return filter.getUserId() != null
                && filter.getKeyword() == null
                && filter.getIsActive() == null
                && filter.getFromDate() == null
                && filter.getToDate() == null;
    }

    public boolean hasAccess(Principal principal, String categoryCode) {
        User user = getUserFromPrincipal(principal);
        log.info("Checking access for user: {} ({}) to category: {}", user.getId(), user.getEmail(), categoryCode);
        
        return userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(user.getId(), categoryCode, 1);
    }

    private PurchaseHistoryResponse convertToResponse(UserPurchasedEntity entity) {
        PurchaseHistoryResponse response = new PurchaseHistoryResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setUserEmail(entity.getUserEmail());
        response.setCategoryCode(entity.getCategoryCode());
        
        // Safely access QuizCategory (might be lazy loaded)
        try {
            QuizCategoryEntity category = entity.getQuizCategory();
            if (category != null) {
                response.setCategoryTitle(category.getTitle());
                response.setCategoryImageUri(category.getImageUri());
            } else {
                response.setCategoryTitle(entity.getCategoryCode().toUpperCase());
                response.setCategoryImageUri(null);
            }
        } catch (Exception e) {
            // Handle LazyInitializationException or any other exception
            log.warn("Could not fetch QuizCategory for code: {}. Error: {}", 
                    entity.getCategoryCode(), e.getMessage());
            response.setCategoryTitle(entity.getCategoryCode().toUpperCase());
            response.setCategoryImageUri(null);
        }
        
        response.setIsPurchased(entity.getIsPurchased() == 1);
        response.setFromTime(entity.getFromTime());
        response.setToTime(entity.getToTime());
        
        boolean isActive = isPurchaseActive(entity);
        response.setIsActive(isActive);
        
        if (entity.getToTime() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getToTime());
            response.setDaysRemaining((int) daysRemaining);
        } else {
            response.setDaysRemaining(null);
        }
        
        return response;
    }
    
    private boolean isPurchaseActive(UserPurchasedEntity entity) {
        log.debug("Checking active status for purchase {}: isPurchased={}, toTime={}", 
                 entity.getId(), entity.getIsPurchased(), entity.getToTime());
        
        if (entity.getIsPurchased() != 1) {
            log.debug("Purchase {} is not active: isPurchased != 1 (value: {})", 
                     entity.getId(), entity.getIsPurchased());
            return false;
        }
        
        if (entity.getToTime() == null) {
            log.debug("Purchase {} is permanent (no expiry)", entity.getId());
            return true;
        }
        
        boolean isAfterNow = entity.getToTime().isAfter(LocalDateTime.now());
        log.debug("Purchase {} time check: toTime={}, now={}, isAfterNow={}", 
                 entity.getId(), entity.getToTime(), LocalDateTime.now(), isAfterNow);
        
        return isAfterNow;
    }
    
    private User getUserFromPrincipal(Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return userOpt.get();
    }
}
