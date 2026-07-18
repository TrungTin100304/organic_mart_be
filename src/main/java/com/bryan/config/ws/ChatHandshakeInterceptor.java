package com.bryan.config.ws;

import com.bryan.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String query = servletRequest.getServletRequest().getQueryString();
            if (query != null && !query.isEmpty()) {
                Map<String, String> params = parseQueryParams(query);
                token = params.get("token");
            }
        }

        if (token == null || token.isBlank()) {
            log.warn("WebSocket handshake rejected: missing token");
            return false;
        }

        try {
            Claims claims = jwtUtils.validateAccessToken(token);

            String email = claims.getSubject();
            Long userId = claims.get("id", Long.class);
            Object rolesObj = claims.get("roles");

            boolean isAdmin = false;
            if (rolesObj instanceof java.util.List<?> roles) {
                isAdmin = roles.stream()
                        .anyMatch(r -> "ROLE_ADMIN".equals(r.toString()));
            }

            attributes.put("userId", userId);
            attributes.put("email", email);
            attributes.put("isAdmin", isAdmin);

            log.info("WebSocket handshake accepted: userId={}, email={}, isAdmin={}", userId, email, isAdmin);
            return true;

        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: invalid token - {}", e.getMessage());
            return false;
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            String key;
            String value;
            try {
                key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8.name());
                value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                continue;
            }
            params.put(key, value);
        }
        return params;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }
}
