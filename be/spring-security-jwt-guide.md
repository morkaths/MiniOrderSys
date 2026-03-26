# Script Spring Security + JWT trong MiniOrderSys BE

Tài liệu này giải thích cách dự án MiniOrderSys BE đang triển khai bảo mật bằng Spring Security và JWT.

## 1) Mục tiêu bảo mật của dự án

Dự án đang theo mô hình API stateless:

- Người dùng đăng ký/đăng nhập qua API auth.
- Server phát JWT sau khi xác thực thành công.
- Các request tiếp theo gửi JWT trong header `Authorization: Bearer <token>`.
- Spring Security kiểm tra token ở mỗi request, không dùng session server-side.

Ý nghĩa chính:

- Không phải lưu session trong server.
- Dễ scale backend theo chiều ngang.
- Tách rõ phần xác thực (authentication) và phân quyền (authorization).

---

## 2) Các thành phần chính trong dự án

### 2.1 SecurityConfig

`SecurityConfig` là nơi khai báo luật bảo mật trung tâm.

Các điểm chính trong code:

- Tắt CSRF cho API stateless.
- Cho phép H2 console chạy với frame same-origin.
- Đặt session policy là `STATELESS`.
- Cấu hình route nào public, route nào cần quyền.
- Gắn `JwtAuthenticationFilter` chạy trước `UsernamePasswordAuthenticationFilter`.
- Đăng ký `DaoAuthenticationProvider` + `PasswordEncoder` + `AuthenticationManager` làm bean.

### 2.2 AuthController + AuthService

`AuthController` cung cấp 2 endpoint:

- `POST /api/auth/register`
- `POST /api/auth/login`

`AuthService` xử lý nghiệp vụ:

- Register: kiểm tra username trùng, mã hóa mật khẩu bằng BCrypt, lưu user mới, phát token.
- Login: gọi `AuthenticationManager.authenticate(...)`, tìm user, phát token.

Kết quả trả về là `AuthResponse` gồm token, tokenType, username, role.

### 2.3 AppUser + AppUserDetailsService

- `AppUser` implement `UserDetails` để Spring Security hiểu user domain của dự án.
- `getAuthorities()` trả về quyền dạng `ROLE_USER` hoặc `ROLE_ADMIN`.
- `AppUserDetailsService` implement `UserDetailsService`, load user từ DB theo username.

Đây là cầu nối giữa Spring Security và dữ liệu user trong bảng `users`.

### 2.4 JwtService

`JwtService` phụ trách toàn bộ thao tác với token:

- `generateToken(UserDetails userDetails)` tạo JWT.
- `extractUsername(token)` lấy subject từ token.
- `isTokenValid(token, userDetails)` kiểm tra username khớp + chưa hết hạn.

Token đang chứa:

- `subject`: username
- `roles`: danh sách authority
- `issuedAt`, `expiration`
- chữ ký HMAC SHA bằng secret trong config

### 2.5 JwtAuthenticationFilter

`JwtAuthenticationFilter` (extends `OncePerRequestFilter`) chạy ở mọi request:

1. Đọc header `Authorization`.
2. Nếu không có Bearer token thì bỏ qua.
3. Nếu có token: parse username từ JWT.
4. Load user bằng `UserDetailsService`.
5. Validate token.
6. Nếu hợp lệ thì set `Authentication` vào `SecurityContextHolder`.

Từ thời điểm này, Spring xem request đã đăng nhập và có thể kiểm tra quyền.

---

## 3) Cấu hình phân quyền hiện tại

Trong `SecurityConfig`, dự án đang phân quyền như sau:

- Public (`permitAll`):
    - `/api/auth/**`
    - `/h2-console/**`
    - `/api/products/**`
    - `/api/orders/**`
    - `/api/tables/**`
    - `/api/invoices/**`
    - `/api/vnpay/**`
    - `/ws/**`
- Chỉ ADMIN:
    - `/api/admin/**`
- Chỉ cần đăng nhập:
    - `/api/vouchers/**`
- USER hoặc ADMIN:
    - `/api/user/**`
- Mọi route còn lại: bắt buộc authenticated.

Lưu ý: rule được kiểm tra theo thứ tự khai báo trong block `authorizeHttpRequests(...)`.

---

## 4) Luồng đăng ký và đăng nhập

### 4.1 Register

1. Client gọi `POST /api/auth/register`.
2. `AuthService` kiểm tra username đã tồn tại chưa.
3. Mật khẩu được mã hóa bằng `PasswordEncoder` (BCrypt).
4. Lưu user mới với role mặc định `USER`.
5. Tạo JWT và trả về cho client.

### 4.2 Login

1. Client gọi `POST /api/auth/login`.
2. `AuthenticationManager` xác thực username/password.
3. Nếu hợp lệ, tải user từ DB.
4. Tạo JWT mới và trả về cho client.

---

## 5) Luồng xác thực JWT cho mỗi request

Với endpoint cần đăng nhập, luồng xử lý thực tế là:

1. Client gửi `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter` tách token và parse username.
3. Filter gọi `userDetailsService.loadUserByUsername(username)`.
4. `JwtService.isTokenValid(...)` kiểm tra token.
5. Nếu hợp lệ, filter set `UsernamePasswordAuthenticationToken` vào `SecurityContextHolder`.
6. Spring Security dùng thông tin này để quyết định truy cập theo rule phân quyền.

Nếu token sai, hết hạn, hoặc không hợp lệ thì request không được xác thực thành công.

---

## 6) JWT secret và thời hạn token

Dự án bind config qua `AppProperties` với prefix `app`.

Trong `application.yaml`:

- `app.jwt.secret`: khóa bí mật để ký token.
- `app.jwt.expiration-ms`: thời gian sống token (hiện tại là `86400000`, tương đương 24 giờ).

`JwtService` dùng các giá trị này để ký và kiểm tra token.

---

## 7) Vai trò của các Bean Security

Các bean quan trọng trong security context:

- `SecurityFilterChain`: định nghĩa toàn bộ rule web security.
- `AuthenticationProvider` (`DaoAuthenticationProvider`): xác thực username/password bằng `UserDetailsService` + `PasswordEncoder`.
- `PasswordEncoder` (`BCryptPasswordEncoder`): mã hóa mật khẩu.
- `AuthenticationManager`: điều phối quá trình authenticate.
- `JwtAuthenticationFilter`: kiểm tra JWT ở tầng filter.

Toàn bộ được Spring IoC quản lý và inject tự động.

---

## 8) Điểm mạnh của cách triển khai hiện tại

- Cấu trúc chuẩn: controller -> service -> security filter rõ ràng.
- Dùng constructor injection nhất quán.
- JWT logic tách riêng trong `JwtService`, dễ bảo trì.
- Rule phân quyền tập trung tại `SecurityConfig`, dễ kiểm soát.
- Password được hash bằng BCrypt thay vì lưu plain text.

---

## 9) Lưu ý cải tiến bảo mật (nên cân nhắc)

- Không nên hard-code JWT secret trong file config commit lên repo.
    - Nên dùng biến môi trường hoặc secret manager.
- Có thể bổ sung cơ chế refresh token để tăng UX và bảo mật phiên đăng nhập.
- Cân nhắc blacklist/revoke token khi logout hoặc đổi mật khẩu.
- Thêm kiểm tra claim nâng cao (issuer, audience) nếu hệ thống mở rộng.

---

## Tổng kết để thuyết trình nhanh

- Spring Security giữ vai trò kiểm soát truy cập và phân quyền endpoint.
- JWT là bằng chứng đăng nhập stateless đi kèm mỗi request.
- `AuthService` phát token, `JwtAuthenticationFilter` xác thực token, `SecurityConfig` quyết định ai được vào route nào.
- Cách triển khai hiện tại phù hợp cho API backend và đã có nền tảng tốt để mở rộng thêm refresh token/revoke token sau này.
