# JWT Nimbus Spring Boot Demo

Project thực hành theo bài giảng **Demo JWT với Spring Boot 3 - Security 6**. Phần tạo, ký và kiểm tra JWT được triển khai bằng **Nimbus JOSE + JWT**, thay cho JJWT. Mật khẩu được mã hóa bằng BCrypt; API dùng bearer token và stateless session.

## Yêu cầu

- JDK 17 trở lên
- Maven 3.6+ (Spring Tool Suite cũng có thể dùng Maven tích hợp)
- Spring Tool Suite 4 trở lên

## Mở bằng Spring Tool Suite

1. Giải nén project.
2. Chọn **File → Import → Maven → Existing Maven Projects**.
3. Chọn thư mục `jwt-nimbus-spring` và nhấn **Finish**.
4. Chờ Maven tải dependency, sau đó chạy `JwtNimbusApplication.java` bằng **Run As → Spring Boot App**.

Ứng dụng chạy tại `http://localhost:8080`. Giao diện demo ở trang chính; H2 console ở `http://localhost:8080/h2-console` với JDBC URL `jdbc:h2:mem:jwtdemo`, user `sa`, để trống password. Cơ sở dữ liệu H2 trong bộ nhớ sẽ được làm mới khi tắt ứng dụng.

## API trong bài

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/auth/register` | Công khai | Tạo tài khoản với quyền `USER` |
| POST | `/api/auth/login` | Công khai | Xác thực và trả access token JWT |
| GET | `/api/users/me` | Đã đăng nhập | Trả hồ sơ người dùng hiện tại |
| GET | `/api/users` | `ADMIN` | Liệt kê người dùng |

Đăng ký mẫu:

```json
{"username":"alice","email":"alice@example.com","password":"Passw0rd!"}
```

Đăng nhập:

```json
{"username":"alice","password":"Passw0rd!"}
```

Đưa giá trị `accessToken` trong phản hồi vào header của API cần đăng nhập:

```text
Authorization: Bearer <accessToken>
```

Tài khoản đăng ký mới chỉ có quyền `USER`. Để thử `/api/users`, cập nhật bảng `APP_USER_ROLES` trong H2 và đổi `USER` thành `ADMIN` cho user cần cấp quyền, rồi đăng nhập lại để nhận token mới.

## Chạy bằng MySQL (tùy chọn)

Tạo schema `jwt_demo`, cấu hình biến môi trường và chạy với profile `mysql`:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/jwt_demo?createDatabaseIfNotExist=true&serverTimezone=UTC'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-password'
$env:JWT_SECRET = 'replace-with-a-private-random-string-at-least-32-bytes'
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Có thể đặt `JWT_SECRET` khi chạy với H2. Khóa mặc định chỉ dùng cho học tập trên máy cá nhân; không dùng trong môi trường thật. Mọi JWT ở demo này được ký HMAC-SHA256, có issuer, subject, issued-at, expiration, jti và claim roles. Filter xác minh chữ ký, thuật toán, issuer và hạn dùng trước khi nạp người dùng.

## Cấu trúc chính

- `jwt/NimbusJwtService`: tạo và xác minh signed JWT qua Nimbus.
- `security/JwtAuthenticationFilter`: đọc `Authorization: Bearer ...` cho mỗi request.
- `security/SecurityConfig`: cấu hình endpoint công khai/bảo vệ và session stateless.
- `auth/AuthService`, `api/AuthController`: đăng ký, đăng nhập và cấp token.
- `api/UserController`: endpoint hồ sơ và endpoint yêu cầu quyền ADMIN.
- `api/ApiExceptionHandler`: phản hồi lỗi JSON.
- `static/index.html`: demo AJAX đăng ký, đăng nhập và gọi hồ sơ.
