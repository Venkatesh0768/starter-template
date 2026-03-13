# 🛡️ Starter Template Backend

Production-ready Spring Boot 4.x authentication backend with:
- JWT-based stateless auth (access + refresh tokens)
- OTP email verification
- Google & GitHub OAuth2 social login
- Role-based access control (USER / VENDOR / ADMIN)
- Swagger UI documentation

---

## ⚡ Quick Start

### Prerequisites
- Java 21+
- MySQL 8+
- Maven (or use the included `mvnw`)

### 1. Create the MySQL Database

```sql
CREATE DATABASE starter_db;
```

### 2. Configure Environment Variables

Copy `.env.example` → `.env` (or just edit `.env`) and fill in your values:

```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/starter_db
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT (use a strong 256-bit+ secret in production)
JWT_SECRET=your-very-long-random-secret-at-least-32-chars
JWT_EXPIRATION=86400000       # 1 day in ms
JWT_REFRESH_EXPIRATION=604800000  # 7 days in ms

# Email (Gmail App Password — NOT your regular Gmail password)
# Enable 2FA on Gmail, then go to: Google Account → Security → App Passwords → Mail
MAIL_USERNAME=you@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# OTP
OTP_EXPIRATION=300000   # 5 min in ms
OTP_LENGTH=6

# CORS + Frontend
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
APP_FRONTEND_URL=http://localhost:5173

# OAuth2 (see setup guide below)
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

### 3. Run the Application

```bash
# Development (uses application-dev.yml)
./mvnw spring-boot:run

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Build JAR
./mvnw clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The API will be available at `http://localhost:8080`.

---

## 🔑 OAuth2 Setup Guide

### Google

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project (or select an existing one)
3. Navigate to **APIs & Services → Credentials**
4. Click **+ CREATE CREDENTIALS → OAuth client ID**
5. Application type: **Web application**
6. Add **Authorized redirect URI**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Copy **Client ID** and **Client Secret** → paste into `.env`

### GitHub

1. Go to GitHub → **Settings → Developer settings → OAuth Apps**
2. Click **New OAuth App**
3. Fill in:
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Click **Register application**
5. Copy **Client ID** and generate **Client Secret** → paste into `.env`

### Social Login Flow (for your frontend)

```
1. Redirect user to:
   http://localhost:8080/oauth2/authorization/google   (or /github)

2. After consent, backend redirects to:
   http://localhost:5173/oauth2/callback?token=eyJhbGci...

3. Frontend reads `token` from URL query param and stores it as the access token.
```

---

## 📋 Environment Variables Reference

| Variable | Required | Description |
|---|---|---|
| `DB_URL` | ✅ | MySQL JDBC URL |
| `DB_USERNAME` | ✅ | MySQL username |
| `DB_PASSWORD` | ✅ | MySQL password |
| `JWT_SECRET` | ✅ | HMAC-SHA key (≥32 chars in prod) |
| `JWT_EXPIRATION` | ✅ | Access token lifetime (ms) |
| `JWT_REFRESH_EXPIRATION` | ✅ | Refresh token lifetime (ms) |
| `MAIL_USERNAME` | ✅ | Gmail address for OTP emails |
| `MAIL_PASSWORD` | ✅ | Gmail App Password |
| `OTP_EXPIRATION` | ✅ | OTP validity period (ms) |
| `OTP_LENGTH` | ✅ | OTP digit count |
| `CORS_ALLOWED_ORIGINS` | ✅ | Comma-separated allowed origins |
| `APP_FRONTEND_URL` | ✅ | Frontend URL for OAuth2 redirect |
| `GOOGLE_CLIENT_ID` | ⚠️ | Required only if using Google login |
| `GOOGLE_CLIENT_SECRET` | ⚠️ | Required only if using Google login |
| `GITHUB_CLIENT_ID` | ⚠️ | Required only if using GitHub login |
| `GITHUB_CLIENT_SECRET` | ⚠️ | Required only if using GitHub login |

---

## 🌐 API Endpoints

### Auth (`/api/auth`) — No token required

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login → returns `accessToken` + `refreshToken` |
| POST | `/api/auth/verify-otp` | Verify email OTP after signup |
| POST | `/api/auth/resend-otp` | Resend OTP |
| POST | `/api/auth/forgot-password` | Request password reset OTP |
| POST | `/api/auth/reset-password` | Reset password with OTP |
| POST | `/api/auth/refresh-token` | Get new access token using refresh token |
| POST | `/api/auth/logout` | Invalidate refresh token |

### User (`/api/user`) — Requires `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/user/profile` | Get own profile |

### Vendor (`/api/vendor`) — Requires `ROLE_VENDOR` or higher

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/vendor/dashboard` | Vendor dashboard |

### Admin (`/api/admin`) — Requires `ROLE_ADMIN`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/dashboard` | Admin dashboard with stats |

### OAuth2 — Social Login

| Method | Endpoint | Description |
|---|---|---|
| GET | `/oauth2/authorization/google` | Initiate Google login |
| GET | `/oauth2/authorization/github` | Initiate GitHub login |

---

## 🔐 Role Matrix

| Endpoint | USER | VENDOR | ADMIN |
|---|:---:|:---:|:---:|
| `/api/user/**` | ✅ | ✅ | ✅ |
| `/api/vendor/**` | ❌ | ✅ | ✅ |
| `/api/admin/**` | ❌ | ❌ | ✅ |

---

## 📚 API Documentation

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Click **Authorize** in Swagger UI → paste your access token (without `Bearer `) to test authenticated endpoints.

---

## 🗂️ Project Structure

```
src/main/java/org/backend/backend/auth/
├── config/
│   ├── DataInitializer.java       # Seeds roles on startup
│   ├── OpenApiConfig.java         # Swagger/OpenAPI configuration
│   └── SecurityConfig.java        # Spring Security + OAuth2 config
├── controller/
│   ├── AuthController.java        # Public auth endpoints
│   ├── UserController.java        # Authenticated user endpoints
│   ├── AdminController.java       # Admin-only endpoints
│   └── VendorController.java      # Vendor+ endpoints
├── dto/
│   ├── ApiResponse.java           # Generic response wrapper
│   ├── AuthResponse.java          # Login/refresh response with tokens
│   ├── LoginRequest.java          # Login payload
│   ├── LogoutRequest.java         # Logout payload (refreshToken)
│   ├── OTPVerificationRequest.java
│   ├── ResetPasswordRequest.java
│   ├── SignupRequest.java          # Registration payload (validated)
│   └── UserDTO.java               # User data in responses
├── exception/
│   ├── GlobalExceptionHandler.java # Centralized error handling
│   └── [Custom exceptions]
├── model/
│   ├── User.java                  # User entity (supports local + OAuth2)
│   ├── Role.java                  # Role entity
│   ├── RoleType.java              # Enum: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN
│   ├── OTP.java                   # OTP entity
│   └── RefreshToken.java          # Refresh token entity
├── repository/
│   └── [JPA Repositories]
├── security/
│   ├── CustomUserDetailsService.java  # DB user loading for password auth
│   ├── JwtAuthenticationEntryPoint.java # 401 JSON error handler
│   ├── JwtAuthenticationFilter.java    # JWT request filter
│   ├── JwtTokenProvider.java           # JWT create / validate / parse
│   └── oauth2/
│       ├── CustomOAuth2UserService.java        # Loads/creates user from OAuth2
│       ├── OAuth2UserPrincipal.java            # Bridge: OAuth2User + UserDetails
│       ├── OAuth2AuthenticationSuccessHandler.java  # Issues JWT after social login
│       └── OAuth2AuthenticationFailureHandler.java  # Redirect on failure
└── service/
    ├── AuthService.java           # Core auth business logic
    ├── EmailService.java          # OTP email sending
    └── OTPService.java            # OTP generation (SecureRandom) + refresh tokens
```

---

## 🔒 Security Design

- **Passwords** — BCrypt hashed (strength 10), never stored in plain text
- **OTP** — Generated with `SecureRandom`, 5-minute expiry, one-time use
- **JWT** — Roles embedded in claims (no DB lookup per request), short-lived (1 day default)
- **Refresh tokens** — UUID, stored in DB, 7-day expiry, deleted on logout
- **OAuth2 users** — Email verified by provider, password field null (cannot use password login)
- **CORS** — Origins configured via `CORS_ALLOWED_ORIGINS` env variable, never hardcoded
