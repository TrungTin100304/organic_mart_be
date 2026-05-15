# Auth Token Workflow — Spring Boot + Spring Security


---


## Stack auth

- Spring Security 6

- JWT (jjwt library)

- RefreshToken lưu DB

- AccessToken stateless (không lưu DB)


---


## Tổng quan token


```

AccessToken  — sống ngắn (15 phút)  — dùng để gọi API

RefreshToken — sống dài (7 ngày)    — dùng để lấy AccessToken mới

```


---


## Cấu trúc file liên quan


```

├── config/

│   └── SecurityConfig.java          # Cấu hình Spring Security

│

├── filter/

│   └── JwtAuthFilter.java           # Chặn request, validate accessToken

│

├── entity/

│   ├── User.java

│   └── RefreshToken.java            # Lưu refreshToken vào DB

│

├── repository/

│   ├── UserRepository.java

│   └── RefreshTokenRepository.java

│

├── service/

│   ├── AuthService.java             # Interface

│   ├── UserDetailsServiceImpl.java  # Implements UserDetailsService

│   └── impl/

│       └── AuthServiceImpl.java

│

├── dto/

│   ├── request/

│   │   ├── LoginRequest.java

│   │   ├── RefreshTokenRequest.java

│   │   └── ForgotPasswordRequest.java

│   │   └── ResetPasswordRequest.java

│   └── response/

│       ├── AuthResponse.java        # Chứa accessToken + refreshToken

│       └── ApiResponse.java

│

├── utils/

│   └── JwtUtils.java

│

└── controller/

    └── AuthController.java

```


---


## 1. Cài đặt dependency


```xml

<!-- pom.xml -->

<dependencies>

    <!-- Spring Security -->

    <dependency>

        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-security</artifactId>

    </dependency>


    <!-- JWT -->

    <dependency>

        <groupId>io.jsonwebtoken</groupId>

        <artifactId>jjwt-api</artifactId>

        <version>0.12.3</version>

    </dependency>

    <dependency>

        <groupId>io.jsonwebtoken</groupId>

        <artifactId>jjwt-impl</artifactId>

        <version>0.12.3</version>

        <scope>runtime</scope>

    </dependency>

    <dependency>

        <groupId>io.jsonwebtoken</groupId>

        <artifactId>jjwt-jackson</artifactId>

        <version>0.12.3</version>

        <scope>runtime</scope>

    </dependency>

</dependencies>

```


```yaml

# application.yml

jwt:

  secret: your-256-bit-secret-key-here-must-be-long-enough

  access-token-expiration: 900000       # 15 phút (ms)

  refresh-token-expiration: 604800000   # 7 ngày (ms)


spring:

  mail:

    host: smtp.gmail.com

    port: 587

    username: your-email@gmail.com

    password: your-app-password

    properties:

      mail.smtp.auth: true

      mail.smtp.starttls.enable: true

```


---


## 2. Entity


```java

// entity/RefreshToken.java

@Entity

@Table(name = "refresh_tokens")

@Getter @Setter @NoArgsConstructor

public class RefreshToken {


    @Id

    @GeneratedValue(strategy = GenerationType.UUID)

    private String id;


    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id", nullable = false)

    private User user;


    @Column(nullable = false, unique = true)

    private String token;


    @Column(nullable = false)

    private LocalDateTime expiresAt;


    @CreationTimestamp

    private LocalDateTime createdAt;


    public boolean isExpired() {

        return LocalDateTime.now().isAfter(expiresAt);

    }

}

```


```java

// entity/User.java — thêm field reset password

@Entity

@Table(name = "users")

@Getter @Setter @NoArgsConstructor

public class User {


    @Id

    @GeneratedValue(strategy = GenerationType.UUID)

    private String id;


    @Column(nullable = false, unique = true)

    private String email;


    private String passwordHash;


    @Enumerated(EnumType.STRING)

    private Role role;


    // Dùng cho forgot password

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpiresAt;


    @CreationTimestamp

    private LocalDateTime createdAt;

}

```


---


## 3. Repository


```java

// repository/RefreshTokenRepository.java

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);

}

```


```java

// repository/UserRepository.java

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);

    boolean existsByEmail(String email);

}

```


---


## 4. JwtUtils


```java

// utils/JwtUtils.java

@Component

public class JwtUtils {


    @Value("${jwt.secret}")

    private String secret;


    @Value("${jwt.access-token-expiration}")

    private long accessTokenExpiration;


    @Value("${jwt.refresh-token-expiration}")

    private long refreshTokenExpiration;


    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    }


    public String generateAccessToken(UserDetails userDetails) {

        return Jwts.builder()

            .subject(userDetails.getUsername())

            .claim("type", "access")

            .issuedAt(new Date())

            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))

            .signWith(getSigningKey())

            .compact();

    }


    public String generateRefreshToken(UserDetails userDetails) {

        return Jwts.builder()

            .subject(userDetails.getUsername())

            .claim("type", "refresh")

            .issuedAt(new Date())

            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))

            .signWith(getSigningKey())

            .compact();

    }


    public String extractEmail(String token) {

        return extractClaims(token).getSubject();

    }


    public boolean isValid(String token, UserDetails userDetails) {

        try {

            String email = extractEmail(token);

            return email.equals(userDetails.getUsername()) && !isExpired(token);

        } catch (JwtException e) {

            return false;

        }

    }


    public boolean isExpired(String token) {

        return extractClaims(token).getExpiration().before(new Date());

    }


    private Claims extractClaims(String token) {

        return Jwts.parser()

            .verifyWith(getSigningKey())

            .build()

            .parseSignedClaims(token)

            .getPayload();

    }

}

```


---


## 5. UserDetailsServiceImpl


```java

// service/UserDetailsServiceImpl.java

@Service

@RequiredArgsConstructor

public class UserDetailsServiceImpl implements UserDetailsService {


    private final UserRepository userRepository;


    @Override

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)

            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));


        return org.springframework.security.core.userdetails.User.builder()

            .username(user.getEmail())

            .password(user.getPasswordHash())

            .roles(user.getRole().name())

            .build();

    }

}

```


---


## 6. JwtAuthFilter


```java

// filter/JwtAuthFilter.java

@Component

@RequiredArgsConstructor

public class JwtAuthFilter extends OncePerRequestFilter {


    private final JwtUtils jwtUtils;

    private final UserDetailsServiceImpl userDetailsService;


    @Override

    protected void doFilterInternal(HttpServletRequest request,

                                    HttpServletResponse response,

                                    FilterChain chain)

            throws ServletException, IOException {


        String header = request.getHeader("Authorization");


        if (header == null || !header.startsWith("Bearer ")) {

            chain.doFilter(request, response);

            return;

        }


        String token = header.substring(7);


        try {

            String email = jwtUtils.extractEmail(token);


            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);


                if (jwtUtils.isValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken auth =

                        new UsernamePasswordAuthenticationToken(

                            userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);

                }

            }

        } catch (ExpiredJwtException e) {

            // Trả 401 rõ ràng để client biết cần refresh

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("{\"success\":false,\"message\":\"Token expired\"}");

            return;

        } catch (JwtException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("{\"success\":false,\"message\":\"Invalid token\"}");

            return;

        }


        chain.doFilter(request, response);

    }

}

```


---


## 7. SecurityConfig


```java

// config/SecurityConfig.java

@Configuration

@EnableWebSecurity

@RequiredArgsConstructor

public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;

    private final UserDetailsServiceImpl userDetailsService;


    @Bean

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http

            .csrf(AbstractHttpConfigurer::disable)


            .sessionManagement(session ->

                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


            .authorizeHttpRequests(auth -> auth

                // Public endpoints — không cần token

                .requestMatchers(

                    "/api/v1/auth/login",

                    "/api/v1/auth/refresh",

                    "/api/v1/auth/forgot-password",

                    "/api/v1/auth/reset-password"

                ).permitAll()

                // Tất cả còn lại phải authenticate

                .anyRequest().authenticated()

            )


            .userDetailsService(userDetailsService)


            // Chạy JwtAuthFilter trước UsernamePasswordAuthenticationFilter

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)


            .build();

    }


    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }


    @Bean

    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)

            throws Exception {

        return config.getAuthenticationManager();

    }

}

```


---


## 8. DTO


```java

// dto/request/LoginRequest.java

public record LoginRequest(

    @NotBlank @Email String email,

    @NotBlank String password

) {}


// dto/request/RefreshTokenRequest.java

public record RefreshTokenRequest(

    @NotBlank String refreshToken

) {}


// dto/request/ForgotPasswordRequest.java

public record ForgotPasswordRequest(

    @NotBlank @Email String email

) {}


// dto/request/ResetPasswordRequest.java

public record ResetPasswordRequest(

    @NotBlank String token,

    @NotBlank @Size(min = 8) String newPassword

) {}


// dto/response/AuthResponse.java

public record AuthResponse(

    String accessToken,

    String refreshToken,

    String email,

    String role

) {}

```


---


## 9. AuthService Interface


```java

// service/AuthService.java

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

}

```


---


## 10. AuthServiceImpl


```java

// service/impl/AuthServiceImpl.java

@Service

@RequiredArgsConstructor

@Transactional

public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final UserDetailsServiceImpl userDetailsService;

    private final JavaMailSender mailSender;


    @Value("${jwt.refresh-token-expiration}")

    private long refreshTokenExpiration;


    // ─── LOGIN ────────────────────────────────────────────────────────────


    @Override

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())

            .orElseThrow(() -> new BadRequestException("Invalid email or password"));


        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {

            throw new BadRequestException("Invalid email or password");

        }


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());


        String accessToken  = jwtUtils.generateAccessToken(userDetails);

        String refreshToken = jwtUtils.generateRefreshToken(userDetails);


        // Xoá refreshToken cũ nếu có, tạo mới

        refreshTokenRepository.deleteByUser(user);

        saveRefreshToken(user, refreshToken);


        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());

    }


    // ─── REFRESH TOKEN ────────────────────────────────────────────────────


    @Override

    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken saved = refreshTokenRepository.findByToken(request.refreshToken())

            .orElseThrow(() -> new BadRequestException("Invalid refresh token"));


        if (saved.isExpired()) {

            refreshTokenRepository.delete(saved);

            throw new BadRequestException("Refresh token expired, please login again");

        }


        User user = saved.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());


        // Rotation: xoá cũ, tạo cả 2 token mới

        refreshTokenRepository.delete(saved);


        String newAccessToken  = jwtUtils.generateAccessToken(userDetails);

        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);


        saveRefreshToken(user, newRefreshToken);


        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole().name());

    }


    // ─── LOGOUT ───────────────────────────────────────────────────────────


    @Override

    public void logout(String refreshToken) {

        refreshTokenRepository.findByToken(refreshToken)

            .ifPresent(refreshTokenRepository::delete);

        // AccessToken tự expire sau 15 phút — không cần xử lý thêm

    }


    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────


    @Override

    public void forgotPassword(ForgotPasswordRequest request) {

        // Luôn trả về thành công dù email có tồn tại hay không

        // → tránh lộ thông tin email nào đã đăng ký

        userRepository.findByEmail(request.email()).ifPresent(user -> {

            String resetToken = UUID.randomUUID().toString();

            user.setResetPasswordToken(resetToken);

            user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));

            userRepository.save(user);

            sendResetEmail(user.getEmail(), resetToken);

        });

    }


    // ─── RESET PASSWORD ───────────────────────────────────────────────────


    @Override

    public void resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByResetPasswordToken(request.token())

            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));


        if (LocalDateTime.now().isAfter(user.getResetPasswordExpiresAt())) {

            throw new BadRequestException("Reset token expired");

        }


        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        user.setResetPasswordToken(null);

        user.setResetPasswordExpiresAt(null);

        userRepository.save(user);


        // Revoke toàn bộ refreshToken — buộc login lại trên mọi thiết bị

        refreshTokenRepository.deleteByUser(user);

    }


    // ─── PRIVATE ──────────────────────────────────────────────────────────


    private void saveRefreshToken(User user, String token) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);

        refreshToken.setToken(token);

        refreshToken.setExpiresAt(

            LocalDateTime.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS));

        refreshTokenRepository.save(refreshToken);

    }


    private void sendResetEmail(String email, String token) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("Reset your password");

        message.setText("Click the link to reset your password (valid 15 minutes):\n\n"

            + "https://yourapp.com/reset-password?token=" + token);

        mailSender.send(message);

    }

}

```


---


## 11. AuthController


```java

// controller/AuthController.java

@RestController

@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor

public class AuthController {


    private final AuthService authService;


    @PostMapping("/login")

    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        return ApiResponse.success(authService.login(request));

    }


    @PostMapping("/refresh")

    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {

        return ApiResponse.success(authService.refresh(request));

    }


    @PostMapping("/logout")

    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void logout(@Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.refreshToken());

    }


    @PostMapping("/forgot-password")

    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ApiResponse.success(null); // Luôn trả success dù email có tồn tại hay không

    }


    @PostMapping("/reset-password")

    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ApiResponse.success(null);

    }

}

```


---


## Workflow Login


```

Client                              Server

  │                                    │

  │  POST /api/v1/auth/login           │

  │  { email, password }               │

  │ ─────────────────────────────────► │

  │                                    │  1. Tìm user theo email

  │                                    │  2. So sánh password với BCrypt hash

  │                                    │  3. Xoá refreshToken cũ (nếu có)

  │                                    │  4. Tạo accessToken  (15 phút)

  │                                    │  5. Tạo refreshToken (7 ngày)

  │                                    │  6. Lưu refreshToken vào DB

  │  200 OK                            │

  │  { accessToken, refreshToken }     │

  │ ◄───────────────────────────────── │

```


## Workflow Refresh Token


```

Client                              Server

  │                                    │

  │  [accessToken hết hạn → 401]       │

  │                                    │

  │  POST /api/v1/auth/refresh         │

  │  { refreshToken }                  │

  │ ─────────────────────────────────► │

  │                                    │  1. Tìm refreshToken trong DB

  │                                    │  2. Kiểm tra còn hạn

  │                                    │  3. Xoá refreshToken cũ (rotation)

  │                                    │  4. Tạo accessToken mới

  │                                    │  5. Tạo refreshToken mới

  │                                    │  6. Lưu refreshToken mới vào DB

  │  200 OK                            │

  │  { accessToken mới, refreshToken mới }

  │ ◄───────────────────────────────── │

```


## Workflow Logout


```

Client                              Server

  │                                    │

  │  POST /api/v1/auth/logout          │

  │  { refreshToken }                  │

  │ ─────────────────────────────────► │

  │                                    │  1. Tìm và xoá refreshToken khỏi DB

  │                                    │  (accessToken tự expire sau 15 phút)

  │  204 No Content                    │

  │ ◄───────────────────────────────── │

  │                                    │

  │  Xoá cả 2 token khỏi bộ nhớ       │

```


## Workflow Forgot Password


```

Client                              Server

  │                                    │

  │  POST /api/v1/auth/forgot-password │

  │  { email }                         │

  │ ─────────────────────────────────► │

  │                                    │  1. Tìm user theo email

  │                                    │  2. Tạo resetToken (UUID)

  │                                    │  3. Lưu resetToken + expiresAt (15 phút)

  │                                    │  4. Gửi email chứa link reset

  │  200 OK (luôn trả success)         │  (Nếu email không tồn tại → bỏ qua,

  │ ◄───────────────────────────────── │   không báo lỗi → tránh lộ email)

  │                                    │

  │  [User nhấn link trong email]      │

  │                                    │

  │  POST /api/v1/auth/reset-password  │

  │  { token, newPassword }            │

  │ ─────────────────────────────────► │

  │                                    │  1. Tìm user theo resetToken

  │                                    │  2. Kiểm tra chưa hết 15 phút

  │                                    │  3. Hash mật khẩu mới

  │                                    │  4. Xoá resetToken

  │                                    │  5. Revoke toàn bộ refreshToken

  │                                    │     (buộc login lại mọi thiết bị)

  │  200 OK                            │

  │ ◄───────────────────────────────── │

```


---


## Database schema


```sql

CREATE TABLE refresh_tokens (

    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    token       TEXT NOT NULL UNIQUE,

    expires_at  TIMESTAMP NOT NULL,

    created_at  TIMESTAMP DEFAULT NOW()

);


-- Thêm vào bảng users

ALTER TABLE users ADD COLUMN reset_password_token       TEXT;

ALTER TABLE users ADD COLUMN reset_password_expires_at  TIMESTAMP;

```


---


## Checklist implement


- [ ] Thêm dependency jjwt vào `pom.xml`

- [ ] Cấu hình `jwt.*` và `spring.mail.*` trong `application.yml`

- [ ] `RefreshToken` entity + `RefreshTokenRepository`

- [ ] Thêm `resetPasswordToken` và `resetPasswordExpiresAt` vào `User` entity

- [ ] `JwtUtils` — generate, validate, extract

- [ ] `UserDetailsServiceImpl` — implements `UserDetailsService`

- [ ] `JwtAuthFilter` — validate accessToken, trả 401 rõ ràng khi expired

- [ ] `SecurityConfig` — permit auth endpoints, stateless session, thêm filter

- [ ] `AuthService` interface + `AuthServiceImpl`

- [ ] `AuthController` — 5 endpoints

- [ ] Test: login → gọi API → để token expire → refresh → logout