package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Request để thanh toán từ Firebase Order.
 * Dùng cho luồng thống nhất Web/App.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request thanh toán từ Firebase Order")
public class FirebasePaymentRequest {

    @NotBlank(message = "Firebase Order ID is required")
    @Schema(description = "Firebase document ID của order", example = "abc123xyz", required = true)
    private String firebaseOrderId;
}
