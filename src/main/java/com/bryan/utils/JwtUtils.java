    package com.bryan.utils;

    import com.bryan.security.CustomUserDetails;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.JwtException;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.security.Keys;
    import jakarta.annotation.PostConstruct;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Component;

    import javax.crypto.SecretKey;
    import java.nio.charset.StandardCharsets;
    import java.util.Date;
    import java.util.List;
    import java.util.stream.Collectors;

    @Component
    public class JwtUtils {

        @Value("${jwt.secret}")
        private String secret;

        @Value("${jwt.access-token-expiration}")
        private long accessTokenExpiration;

        @Value("${jwt.refresh-token-expiration}")
        private long refreshTokenExpiration;

        // ─── Validate secret tại startup ────────────────────────────────────────────
        @PostConstruct
        public void validateSecret() {
            if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException(
                        "jwt.secret must be at least 32 bytes (256-bit) for HS256"
                );
            }
        }

        // ─── Signing Key ─────────────────────────────────────────────────────────────
        private SecretKey getSigningKey() {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        // ─── Generate Tokens ─────────────────────────────────────────────────────────

        public String generateAccessToken(UserDetails userDetails) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return Jwts.builder()
                    .subject(userDetails.getUsername())
                    .claim("type", "access")
                    .claim("roles", roles)
                    .claim("id", ((CustomUserDetails) userDetails).getId())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .signWith(getSigningKey())
                    .compact();
        }

        public String generateRefreshToken(UserDetails userDetails) {
            // Refresh token KHÔNG embed roles — chỉ dùng để đổi access token mới
            return Jwts.builder()
                    .subject(userDetails.getUsername())
                    .claim("type", "refresh")
                    .claim("id", ((CustomUserDetails) userDetails).getId())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                    .signWith(getSigningKey())
                    .compact();
        }

        // ─── Validate & Extract ───────────────────────────────────────────────────────


        public Claims validateAndExtractClaims(String token) {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        /**
         * Validate access token — thêm guard check type = "access".
         * Dùng trong JwtAuthFilter.
         */
        public Claims validateAccessToken(String token) {
            Claims claims = validateAndExtractClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                throw new JwtException("Token is not an access token");
            }
            return claims;
        }

        /**
         * Validate refresh token — thêm guard check type = "refresh".
         * Dùng trong AuthService khi đổi token mới.
         */
        public Claims validateRefreshToken(String token) {
            Claims claims = validateAndExtractClaims(token);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new JwtException("Token is not a refresh token");
            }
            return claims;
        }


        public String extractEmail(String token) {
            return validateAndExtractClaims(token).getSubject();
        }
    }