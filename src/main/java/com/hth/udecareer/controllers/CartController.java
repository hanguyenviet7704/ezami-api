package com.hth.udecareer.controllers;

import java.security.Principal;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.request.CartRequest;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.CartResponse;
import com.hth.udecareer.service.CartService;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Shopping Cart Management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @Operation(summary = "Lấy thông tin giỏ hàng của User")
    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(cartService.getCartDetails(userId));
    }

    @Operation(
            summary = "Thêm sản phẩm vào giỏ hàng",
            description = """
                    **Thêm một gói dịch vụ vào giỏ hàng của người dùng.**
                    
                    Hệ thống sẽ kiểm tra tính hợp lệ của Category và Plan trước khi thêm.
                    
                    **1. categoryCode (Mã môn học):**
                    - `psm1`, `psm2` 
                    - `pspo1`, `pspo2` 
                    - `istqb_foundation`, `istqb_agile`, 
                    - `istqb_adv_ta`, `istqb_adv_tm`, `istqb_adv_tta`
                    - `cbap`, `ccba`, `ecba` 
                    
                    **2. planCode (Gói thời gian):**
                    - `PLAN_30`: Gói 30 ngày.
                    - `PLAN_90`: Gói 90 ngày.
                    """
    )
    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse> addToCart(@RequestBody @Valid CartRequest request, Principal principal) throws AppException {
        // Validate input
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_KEY, "Request body cannot be null");
        }
        if (request.getCategoryCode() == null || request.getCategoryCode().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Category code is required");
        }
        if (request.getPlanCode() == null || request.getPlanCode().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Plan code is required");
        }

        Long userId = getUserIdFromPrincipal(principal);
        cartService.addToCart(userId, request.getCategoryCode().trim(), request.getPlanCode().trim());
        return ResponseEntity.ok(ApiResponse.success("Item added to cart"));
    }

    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/cart/{cartItemId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable Long cartItemId, Principal principal) throws AppException {
        // Validate input
        if (cartItemId == null || cartItemId <= 0) {
            throw new AppException(ErrorCode.INVALID_KEY, "Invalid cart item ID");
        }

        Long userId = getUserIdFromPrincipal(principal);
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    @DeleteMapping("/cart/clear")
    public ResponseEntity<ApiResponse> clearCart(Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }

    private Long getUserIdFromPrincipal(Principal principal) throws AppException {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        if (user.getId() == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        return user.getId();
    }
}
