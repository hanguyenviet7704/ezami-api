package com.hth.udecareer.service;

import com.hth.udecareer.entities.CartItemEntity;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.SubscriptionPlanEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.CartItemResponse;
import com.hth.udecareer.model.response.CartResponse;
import com.hth.udecareer.repository.CartItemRepository;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final SubscriptionPlanRepository planRepository;
    private final QuizCategoryRepository categoryRepository;

    @Transactional
    public void addToCart(Long userId, String categoryCode, String planCode) {
        if (!categoryRepository.existsByCode(categoryCode)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!planRepository.existsByCode(planCode)) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (cartItemRepository.existsByUserIdAndCategoryCodeAndPlanCode(userId, categoryCode, planCode)) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Item already in cart");
        }

        CartItemEntity item = new CartItemEntity();
        item.setUserId(userId);
        item.setCategoryCode(categoryCode);
        item.setPlanCode(planCode);

        cartItemRepository.save(item);
    }

    public CartResponse getCartDetails(Long userId) {
        List<CartItemEntity> items = cartItemRepository.findAllByUserId(userId);

        if (items.isEmpty()) {
            return CartResponse.builder()
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        // Collect all unique codes to batch fetch (fixes N+1 query problem)
        Set<String> planCodes = items.stream()
                .map(CartItemEntity::getPlanCode)
                .collect(Collectors.toSet());
        Set<String> categoryCodes = items.stream()
                .map(CartItemEntity::getCategoryCode)
                .collect(Collectors.toSet());

        // Batch fetch all plans and categories in 2 queries instead of 2*N queries
        Map<String, SubscriptionPlanEntity> planMap = planRepository.findByCodeIn(planCodes)
                .stream()
                .collect(Collectors.toMap(SubscriptionPlanEntity::getCode, p -> p));
        Map<String, QuizCategoryEntity> categoryMap = categoryRepository.findByCodeIn(categoryCodes)
                .stream()
                .collect(Collectors.toMap(QuizCategoryEntity::getCode, c -> c));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItemEntity item : items) {
            SubscriptionPlanEntity plan = planMap.get(item.getPlanCode());
            QuizCategoryEntity category = categoryMap.get(item.getCategoryCode());

            if (plan == null || category == null) {
                // Skip invalid items (orphaned references)
                continue;
            }

            totalAmount = totalAmount.add(plan.getPrice());

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .categoryCode(item.getCategoryCode())
                    .categoryName(category.getTitle())
                    .planCode(item.getPlanCode())
                    .planName(plan.getName())
                    .price(plan.getPrice())
                    .durationDays(plan.getDurationDays())
                    .build());
        }

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        cartItemRepository.deleteByUserIdAndId(userId, cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }
}
