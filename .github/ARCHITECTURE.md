# Project Architecture — Spring Boot (Java)

## Stack
- Java 21
- Spring Boot 3.x
- Spring Data JPA + Hibernate
- Spring Security + JWT
- PostgreSQL
- Maven / Gradle
- MapStruct (entity ↔ DTO)
- Lombok

---

## Cấu trúc package

```
src/main/java/com/yourcompany/yourapp/
│
├── controller/
│   ├── UserController.java
│   └── ProductController.java
│
├── service/
│   ├── UserService.java              # Interface
│   ├── ProductService.java           # Interface
│   └── impl/
│       ├── UserServiceImpl.java      # Implementation
│       └── ProductServiceImpl.java   # Implementation
│
├── repository/
│   ├── UserRepository.java
│   └── ProductRepository.java
│
├── entity/
│   ├── User.java
│   └── Product.java
│
├── dto/
│   ├── request/
│   │   ├── UserRequest.java
│   │   └── ProductRequest.java
│   └── response/
│       ├── UserResponse.java
│       ├── ProductResponse.java
│       └── ApiResponse.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
│
├── filter/
│   └── JwtAuthFilter.java
│
├── config/
│   ├── SecurityConfig.java
│   └── AppConfig.java
│
├── utils/
│   ├── JwtUtils.java
│   └── DateUtils.java
│
└── YourAppApplication.java
```

---

## Quy tắc dependency

```
controller → service (interface) → impl → repository → entity
     ↓              ↓
    dto          exception
                 utils (dùng được ở mọi nơi)
                 config / filter (standalone)
```

- `controller` chỉ inject `UserService` (interface), **không biết** `UserServiceImpl` tồn tại
- `impl` là nơi duy nhất chứa business logic
- `service` interface chỉ khai báo method, không có logic
- `utils` không import bất kỳ package nào trong project
- `config` và `filter` không chứa business logic

---

## Tại sao dùng Interface + Impl?

| Lợi ích | Giải thích |
|---------|-----------|
| Dễ test | Mock interface thay vì class thật khi viết unit test |
| Dễ swap | Thay implementation mà không sửa controller |
| Rõ contract | Interface = bản cam kết "service này làm được gì" |
| Spring best practice | `@Autowired` theo interface, không theo class cụ thể |

---

## Từng package

### `entity/`
Ánh xạ trực tiếp với database table. Chỉ chứa field và annotation JPA, không có logic.
**Lưu ý:** ID sử dụng `long` với `GenerationType.IDENTITY` (auto-increment).

```java
// entity/User.java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

---

### `repository/`
Interface JPA thuần. Chỉ khai báo query, không chứa logic.
**Thay đổi:** `JpaRepository<User, String>` → `JpaRepository<User, Long>`

```java
// repository/UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

### `dto/`
Kiểm soát data vào/ra. Validate input ở Request, che field nhạy cảm ở Response.

```java
// dto/request/UserRequest.java
public record UserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}
```

```java
// dto/response/UserResponse.java
public record UserResponse(
    Long id,                          // ← thay từ String sang Long
    String email,
    Role role,
    LocalDateTime createdAt
) {}
```

```java
// dto/response/ApiResponse.java
public record ApiResponse<T>(
                boolean success,
                int statusCode,
                T data,
                String message
        ) {
    // Trả về thành công với status code mặc định là 200
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, data, "Success");
    }

    // Trả về thành công với status code tùy chỉnh (VD: 201 Created)
    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, data, "Success");
    }

    // Trả về lỗi với status code tùy chỉnh (VD: 400, 404, 500)
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(false, statusCode, null, message);
    }

    // Trả về lỗi với status code mặc định (VD: 500 hoặc 400 tùy logic dự án của bạn)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 500, null, message);
    }
}
```

---

### `service/` — Interface
Khai báo contract. Chỉ có method signature, không có logic.
Controller inject vào đây, không inject Impl trực tiếp.

```java
// service/UserService.java
public interface UserService {
    User createUser(String email, String rawPassword);
    User getById(Long id);                              // ← Long thay vì String
    User updateUser(Long id, String email);             // ← Long thay vì String
    void deleteUser(Long id);                           // ← Long thay vì String
    List<User> getAll();
}
```

---

### `service/impl/` — Implementation
Toàn bộ business logic nằm ở đây. Nhận và trả `entity`, không biết về `dto`.

```java
// service/impl/UserServiceImpl.java
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {                      // ← Long thay vì String
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    public User updateUser(Long id, String email) {     // ← Long thay vì String
        User user = getById(id);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {                   // ← Long thay vì String
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
```

---

### `controller/`
Inject interface → gọi service → map sang DTO → trả response.
Không chứa if/else logic nghiệp vụ.

```java
// controller/UserController.java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;   // ← inject interface, không phải Impl
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User user = userService.createUser(request.email(), request.password());
        return ApiResponse.success(userMapper.toResponse(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {  // ← Long thay vì String
        return ApiResponse.success(userMapper.toResponse(userService.getById(id)));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll().stream()
            .map(userMapper::toResponse)
            .toList();
        return ApiResponse.success(users);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id,      // ← Long thay vì String
                                            @Valid @RequestBody UserRequest request) {
        User user = userService.updateUser(id, request.email());
        return ApiResponse.success(userMapper.toResponse(user));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {         // ← Long thay vì String
        userService.deleteUser(id);
    }
}
```

---

### `exception/`
Tập trung xử lý lỗi. Không để try/catch rải rác ở controller hay service.

```java
// exception/ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
// exception/BadRequestException.java
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(BadRequestException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        return ApiResponse.error("Internal server error");
    }
}
```

---

### `filter/`
Chạy trước controller. Xử lý JWT, logging, rate limiting...

```java
// filter/JwtAuthFilter.java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

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
        String email = jwtUtils.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtUtils.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

---

### `config/`
Khai báo Spring beans và cấu hình. Không chứa logic.

```java
// config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

### `utils/`
Hàm helper. `JwtUtils` cần `@Component` để đọc `@Value`. Còn lại giữ static thuần Java.

```java
// utils/JwtUtils.java
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email) { ... }
    public String extractEmail(String token) { ... }
    public boolean isValid(String token, UserDetails userDetails) { ... }
}
```

```java
// utils/DateUtils.java
public class DateUtils {
    public static String format(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
```

---

## Naming conventions

| File | Convention | Ví dụ |
|------|-----------|-------|
| Entity | PascalCase | `User.java` |
| Repository | Entity + `Repository` | `UserRepository.java` |
| Service interface | Entity + `Service` | `UserService.java` |
| Service impl | Entity + `ServiceImpl` | `UserServiceImpl.java` |
| Controller | Entity + `Controller` | `UserController.java` |
| Request DTO | Entity + `Request` | `UserRequest.java` |
| Response DTO | Entity + `Response` | `UserResponse.java` |
| Mapper | Entity + `Mapper` | `UserMapper.java` |
| Exception | Mô tả lỗi + `Exception` | `ResourceNotFoundException.java` |
| Filter | Mô tả + `Filter` | `JwtAuthFilter.java` |
| Config | Mô tả + `Config` | `SecurityConfig.java` |
| Utils | Mô tả + `Utils` | `JwtUtils.java` |

---

## API URL convention

```
GET    /api/v1/users           # danh sách
GET    /api/v1/users/{id}      # theo id (id là Long)
POST   /api/v1/users           # tạo mới
PUT    /api/v1/users/{id}      # cập nhật toàn bộ
PATCH  /api/v1/users/{id}      # cập nhật một phần
DELETE /api/v1/users/{id}      # xoá
```

**Ví dụ:** `GET /api/v1/users/1`, `GET /api/v1/users/42`

---

## Checklist khi thêm domain mới (ví dụ: Order)

- [ ] `entity/Order.java` — ID là `Long` với `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- [ ] `repository/OrderRepository.java` — `extends JpaRepository<Order, Long>`
- [ ] `service/OrderService.java` — interface, khai báo method (dùng `Long` cho ID)
- [ ] `service/impl/OrderServiceImpl.java` — logic thực sự, `implements OrderService`
- [ ] `dto/request/OrderRequest.java`
- [ ] `dto/response/OrderResponse.java` — ID là `Long`
- [ ] `controller/OrderController.java` — inject `OrderService` (interface), path param ID là `Long`
- [ ] Thêm exception mới vào `exception/` nếu cần

---

## Database Migration (Flyway / Liquibase)

Khi sử dụng `GenerationType.IDENTITY`, cần tạo sequence hoặc auto-increment ở database:

**PostgreSQL:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**MySQL:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Những điều KHÔNG làm

- Controller inject `UserServiceImpl` trực tiếp — luôn inject interface `UserService`
- Đặt `@Transactional` ở controller — chỉ đặt ở `ServiceImpl`
- Logic nghiệp vụ trong interface — interface chỉ khai báo method
- Controller gọi thẳng `repository` — đi qua `service`
- Service nhận hoặc trả `dto` — chỉ dùng `entity`
- Validate bằng if/else trong `ServiceImpl` — dùng annotation trong `dto/request`
- Hardcode secret, URL, config — dùng `application.properties` + `@Value`
- Sử dụng UUID khi có thể dùng auto-increment — `long` + `IDENTITY` hiệu quả hơn và dễ quản lý