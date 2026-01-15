package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.QrTransaction;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.webhook.SePayWebhookDto;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.service.SePayWebhookService;
import com.hth.udecareer.service.UserAccessService;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.service.VnpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookServiceImpl implements SePayWebhookService {

    private final OrderRepository orderRepository;
    private final UserAccessService userAccessService;
    private final QrTransactionService qrTransactionService;
    private final com.hth.udecareer.service.QrPaymentGrantService qrPaymentGrantService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private VnpayService vnpayService;
    @org.springframework.beans.factory.annotation.Value("${sepay.webhookSecret:}")
    private String webhookSecret;

    // include 'order' in content pattern (e.g., "Order48") so we can map content to order ids
    private static final Pattern CONTENT_PATTERN = Pattern.compile("(?i)(order|course|lesson|quiz|topic|post)[^0-9]*(\\d+)");
    private static final Pattern DIGITS_PATTERN = Pattern.compile("(\\d+)");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse process(SePayWebhookDto dto) {
        log.info("Processing SePay webhook: id={}, gateway={}, amount={}, account={}", dto.getId(), dto.getGateway(), dto.getTransferAmount(), dto.getAccountNumber());

        // Only process incoming transfers
        if (!"in".equalsIgnoreCase(dto.getTransferType())) {
            log.info("SePay transfer is not an 'in' transfer, ignored: type={}", dto.getTransferType());
            return ApiResponse.success("Ignored non-in transfer");
        }

        // Step 1: try to match referenceCode to a numeric order id or to other order identifiers
        if (dto.getReferenceCode() != null) {
            String ref = dto.getReferenceCode().trim();
            boolean handledRef = false;
            // Only parse as an explicit order id when the reference code is purely numeric or contains a clear 'order<id>' label.
            if (ref.matches("^\\d+$") || ref.toLowerCase().matches("^order[#: ]?\\d+$")) {
                Matcher m = DIGITS_PATTERN.matcher(ref);
                if (m.find()) {
                    String idStr = m.group(1);
                    try {
                        long orderId = Long.parseLong(idStr);
                        Optional<OrderEntity> optOrder = orderRepository.findById(orderId);
                        if (optOrder.isPresent()) {
                            handledRef = true;
                            OrderEntity order = optOrder.get();
                            long amount = (dto.getTransferAmount() == null ? 0L : dto.getTransferAmount());
                            long orderAmt = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
                            if (!OrderStatus.PAID.equals(order.getStatus()) && orderAmt == amount) {
                            // Mark paid and grant access via SePay webhook
                            String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                            order.setPaymentMethod(pm);
                            order.setStatus(OrderStatus.PAID);
                            orderRepository.save(order);
                            // grant access directly to user via items
                            for (var item: order.getItems()) {
                                userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                            }
                            log.info("Order #{} marked paid via SePay; access granted.", orderId);
                            return ApiResponse.success("Order confirmed and access granted");
                        } else {
                            return ApiResponse.success("Order already paid or amount mismatch");
                            }
                        }
                    } catch (NumberFormatException ex) {
                        log.warn("Reference code contains digits but can't parse as order id: {}", dto.getReferenceCode());
                    }
                }
            }

            // Try to match order by transactionNo or full referenceCode if not already handled
            if (!handledRef) {
                var optByTxn = orderRepository.findByTransactionNo(dto.getReferenceCode());
                if (optByTxn.isPresent()) {
                    OrderEntity order = optByTxn.get();
                    long amount = (dto.getTransferAmount() == null ? 0L : dto.getTransferAmount());
                    long orderAmt = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
                    if (!OrderStatus.PAID.equals(order.getStatus()) && orderAmt == amount) {
                        String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                        order.setPaymentMethod(pm);
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                        // grant access directly to user via items
                        for (var item: order.getItems()) {
                            userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                        }
                        log.info("Order #{} marked paid via SePay (by ref txn) and access granted.", order.getId());
                        return ApiResponse.success("Order confirmed and access granted (by referenceTxn)");
                    } else {
                        return ApiResponse.success("Order already paid or amount mismatch");
                    }
                }
            }
        }

        // Step 2: try to find a QR transaction (if account and amount match)
        if (dto.getAccountNumber() != null && dto.getTransferAmount() != null) {
            String bankAccount = dto.getAccountNumber();
            String amountStr = String.valueOf(dto.getTransferAmount());
            var optTx = qrTransactionService.findByBankAccountAndAmount(bankAccount, amountStr);
            if (optTx.isPresent()) {
                QrTransaction tx = optTx.get();
                try {
                    qrTransactionService.markUsed(tx.getTransactionId(), "sepay");
                    log.info("Marked qr transaction used: {} for SePay webhook", tx.getTransactionId());
                    try {
                        qrPaymentGrantService.processQrPaymentGrant(tx, "sepay");
                    } catch (Exception ex) {
                        log.warn("Failed to process QR grant (sepay): {}", ex.getMessage());
                    }
                    return ApiResponse.success("QR transaction confirmed and marked used");
                } catch (Exception e) {
                    log.warn("Failed to mark QR tx used: {}", e.getMessage());
                    return ApiResponse.fail(500, "Failed to mark QR transaction used");
                }
            }
        }

        // Step 3: parse content to find content type and id, and attempt to find a pending order or grant access based on match
        if (dto.getContent() != null) {
            Matcher m = CONTENT_PATTERN.matcher(dto.getContent());
            if (m.find()) {
                String type = m.group(1).toLowerCase();
                String idStr = m.group(2);
                log.info("SePay content mentions {} {}", type, idStr);

                // If content is an explicit Order<id>, try matching by order id (strongest match)
                if ("order".equalsIgnoreCase(type)) {
                    try {
                        long orderId = Long.parseLong(idStr);
                        var optOrderById = orderRepository.findById(orderId);
                        if (optOrderById.isPresent()) {
                            OrderEntity order = optOrderById.get();
                            long amount = dto.getTransferAmount() == null ? 0L : dto.getTransferAmount();
                            long orderAmt = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
                            if (!OrderStatus.PAID.equals(order.getStatus()) && orderAmt == amount) {
                                String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                                order.setPaymentMethod(pm);
                                order.setStatus(OrderStatus.PAID);
                                orderRepository.save(order);
                                for (var item: order.getItems()) {
                                    userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                                }
                                log.info("Order #{} marked paid via SePay; access granted (content order).", order.getId());
                                return ApiResponse.success("Order confirmed and access granted (content order)");
                            } else {
                                return ApiResponse.success("Order already paid or amount mismatch");
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse order id from content: {}", idStr);
                    }
                }

                // Attempt to find pending order by amount and item mapping for non-order content (e.g., course, quiz)
                long amount = dto.getTransferAmount() == null ? 0L : dto.getTransferAmount();
                var optOrder = orderRepository.findFirstByStatusAndTotalAmountWithItems(OrderStatus.PENDING, java.math.BigDecimal.valueOf(amount));
                if (optOrder.isPresent()) {
                    OrderEntity order = optOrder.get();
                    String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                    order.setPaymentMethod(pm);
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                    for (var item: order.getItems()) {
                        userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                    }
                    return ApiResponse.success("Matched existing pending order by amount and granted access");
                }

                // As last resort: log and respond that we couldn't find an order
                log.warn("Could not match SePay webhook content to any order/purchase: content={}, amount={}", dto.getContent(), dto.getTransferAmount());
                // If content exists but was not matching pattern, fallback: attempt to find any PENDING order that matches amount
                if (dto.getContent() != null && !CONTENT_PATTERN.matcher(dto.getContent()).find()) {
                    var optFallback = orderRepository.findFirstByStatusAndTotalAmountWithItems(OrderStatus.PENDING, java.math.BigDecimal.valueOf(amount));
                    if (optFallback.isPresent()) {
                        OrderEntity order = optFallback.get();
                        String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                        order.setPaymentMethod(pm);
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                        for (var item: order.getItems()) {
                            userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                        }
                        log.info("Matched existing pending order by amount (fallback) and granted access, orderId={}", order.getId());
                        return ApiResponse.success("Matched existing pending order by amount and granted access (fallback)");
                    }

                    // handle explicit Order<id> content (e.g., Order48)
                } else {
                    // Try parsing 'order' from content in a generic way (case-insensitive) if not matched by the main pattern
                    Matcher digitsOnly = DIGITS_PATTERN.matcher(dto.getContent());
                    if (digitsOnly.find()) {
                        try {
                            long orderId = Long.parseLong(digitsOnly.group(1));
                            Optional<OrderEntity> optOrder2 = orderRepository.findById(orderId);
                            if (optOrder2.isPresent()) {
                                OrderEntity order = optOrder2.get();
                                long transferAmt = (dto.getTransferAmount() == null ? 0L : dto.getTransferAmount());
                                long orderAmt = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
                                if (!OrderStatus.PAID.equals(order.getStatus()) && orderAmt == transferAmt) {
                                    String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                                    order.setPaymentMethod(pm);
                                    order.setStatus(OrderStatus.PAID);
                                    orderRepository.save(order);
                                    for (var item: order.getItems()) {
                                        userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                                    }
                                    log.info("Order #{} marked paid via SePay (content Order<id>) and access granted.", order.getId());
                                    return ApiResponse.success("Order confirmed and access granted (content order)");
                                } else {
                                    return ApiResponse.success("Order already paid or amount mismatch");
                                }
                            }
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }

                return ApiResponse.success("No matching order found; manual review needed");
            }
        }

        // Final attempt: if gateway is present and transfer amount matches a PENDING order, mark it as paid
        if (dto.getGateway() != null && dto.getTransferAmount() != null) {
            var optOrderByAmount = orderRepository.findFirstByStatusAndTotalAmountWithItems(OrderStatus.PENDING, java.math.BigDecimal.valueOf(dto.getTransferAmount()));
            if (optOrderByAmount.isPresent()) {
                OrderEntity order = optOrderByAmount.get();
                String pm = dto.getGateway() == null ? "SEPAY" : dto.getGateway().toUpperCase();
                order.setPaymentMethod(pm);
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                for (var item: order.getItems()) {
                    userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                }
                log.info("Matched existing pending order by gateway+amount and granted access, orderId={}", order.getId());
                return ApiResponse.success("Matched existing pending order by amount and granted access (gateway)");
            }
        }

        // No action matched
        log.info("No matching action for SePay webhook: referenceCode={}, content={}", dto.getReferenceCode(), dto.getContent());
        return ApiResponse.success("No match; ignored");
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // signature verification disabled
            return true;
        }
        if (signature == null || signature.isBlank()) return false;
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(webhookSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String base64 = java.util.Base64.getEncoder().encodeToString(hash);
            String base64Url = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            String hexLower = bytesToHex(hash).toLowerCase();
            String hexUpper = bytesToHex(hash).toUpperCase();
            String candidate = signature.trim();
            if (candidate.startsWith("sha256=")) candidate = candidate.substring(7);
            if (candidate.equals(base64) || candidate.equals(base64Url) || candidate.equalsIgnoreCase(hexLower) || candidate.equalsIgnoreCase(hexUpper)) return true;
            return false;
        } catch (Exception e) {
            log.error("Error verifying sepay webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
