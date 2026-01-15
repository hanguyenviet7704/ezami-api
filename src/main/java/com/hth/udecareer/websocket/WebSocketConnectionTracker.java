package com.hth.udecareer.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class WebSocketConnectionTracker {

    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Extract userId từ session attributes (set trong WebSocketConfig)
        String userId = getUserIdFromSession(accessor);

        if (userId != null) {
            connectedUsers.add(userId);
            sessionUserMap.put(sessionId, userId);

            log.info("✅ User {} connected to WebSocket (sessionId: {}). Total online: {}",
                    userId, sessionId, connectedUsers.size());
        } else {
            log.warn("⚠️ WebSocket connected but no userId found in session: {}", sessionId);
        }
    }

    /**
     * Event handler khi WebSocket connection bị disconnect
     */
    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Lấy userId từ map
        String userId = sessionUserMap.remove(sessionId);

        if (userId != null) {
            connectedUsers.remove(userId);

            log.info("🔌 User {} disconnected from WebSocket (sessionId: {}). Total online: {}",
                    userId, sessionId, connectedUsers.size());
        }
    }

    /**
     * Check xem user có đang connected WebSocket không
     */
    public boolean isUserConnected(Long userId) {
        if (userId == null) {
            return false;
        }
        return connectedUsers.contains(userId.toString());
    }

    /**
     * Lấy số lượng users đang online
     */
    public int getConnectedUserCount() {
        return connectedUsers.size();
    }

    /**
     * Lấy list tất cả user IDs đang online
     */
    public Set<String> getConnectedUsers() {
        return Set.copyOf(connectedUsers);
    }

    /**
     * Extract userId từ WebSocket session
     * userId được set trong WebSocketConfig handshake interceptor
     */
    private String getUserIdFromSession(StompHeaderAccessor accessor) {
        try {
            // Cách 1: Từ session attributes
            Object userIdObj = accessor.getSessionAttributes().get("userId");
            if (userIdObj != null) {
                return userIdObj.toString();
            }

            // Cách 2: Từ user principal (nếu authenticated)
            if (accessor.getUser() != null) {
                return accessor.getUser().getName();
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from session: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Manually add user connection (for testing)
     */
    public void addConnection(Long userId) {
        if (userId != null) {
            connectedUsers.add(userId.toString());
        }
    }

    /**
     * Manually remove user connection (for testing)
     */
    public void removeConnection(Long userId) {
        if (userId != null) {
            connectedUsers.remove(userId.toString());
        }
    }

    /**
     * Clear all connections (for testing/cleanup)
     */
    public void clearAllConnections() {
        connectedUsers.clear();
        sessionUserMap.clear();
        log.info("Cleared all WebSocket connections");
    }
}

