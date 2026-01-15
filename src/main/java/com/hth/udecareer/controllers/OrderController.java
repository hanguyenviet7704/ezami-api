package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.response.OrderHistoryResponse;
import com.hth.udecareer.model.response.PagedResponse;
import com.hth.udecareer.model.response.UserResponse;
import com.hth.udecareer.repository.OrderItemRepository;
import com.hth.udecareer.service.OrderService;
import com.hth.udecareer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Order Management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemRepository orderItemRepository;

    @Operation(
            summary = "Xem lịch sử mua hàng của User với phân trang và lọc",
            description = """
                    **Lấy danh sách lịch sử đơn hàng với phân trang, sắp xếp và lọc**
                    
                    **Tham số phân trang:**
                    - page: Số trang (bắt đầu từ 0)
                    - size: Số lượng items trên mỗi trang
                    - sortBy: Trường sắp xếp (createdAt, totalAmount, status)
                    - sortDirection: Hướng sắp xếp (asc hoặc desc)
                    
                    **Tham số lọc:**
                    - status: Trạng thái đơn hàng (PAID, PENDING, FAILED)
                    - fromDate: Ngày bắt đầu (format: yyyy-MM-dd hoặc yyyy-MM-ddTHH:mm:ss)
                    - toDate: Ngày kết thúc (format: yyyy-MM-dd hoặc yyyy-MM-ddTHH:mm:ss)
                    
                    **Ví dụ:**
                    - /api/orders/history?page=0&size=10
                    - /api/orders/history?status=PAID
                    - /api/orders/history?fromDate=2025-01-01&toDate=2025-12-31
                    - /api/orders/history?status=PAID&fromDate=2025-01-01&sortBy=totalAmount&sortDirection=desc
                    """
    )
    @GetMapping("/orders/history")
    public ResponseEntity<PagedResponse<OrderHistoryResponse>> getOrderHistory(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng items trên mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp (createdAt, totalAmount, status)", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc hoặc desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Lọc theo trạng thái (PAID, PENDING, FAILED)", example = "PAID")
            @RequestParam(required = false) String status,
            @Parameter(description = "Lọc từ ngày (yyyy-MM-dd hoặc yyyy-MM-ddTHH:mm:ss)", example = "2025-01-01")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Lọc đến ngày (yyyy-MM-dd hoặc yyyy-MM-ddTHH:mm:ss)", example = "2025-12-31")
            @RequestParam(required = false) String toDate) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (page < 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (size > 100) {
            throw new AppException(ErrorCode.PAGE_SIZE_TOO_LARGE);
        }

        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) {
            throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);
        }
        Long userId = user.getId();

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderHistoryResponse> orderPage = orderService.getOrderHistory(
                userId, pageable, status, fromDate, toDate);

        PagedResponse<OrderHistoryResponse> response = PagedResponse.<OrderHistoryResponse>builder()
                .content(orderPage.getContent())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by order ID")
    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<List<OrderItemEntity>> getOrderItems(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long orderId) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Get all my order items")
    @GetMapping("/orders/items/my")
    public ResponseEntity<List<OrderItemEntity>> getMyOrderItems(
            @Parameter(hidden = true) Principal principal) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) {
            throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);
        }

        List<OrderItemEntity> items = orderItemRepository.findByUserId(user.getId());
        return ResponseEntity.ok(items);
    }
}