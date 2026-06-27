# 🔍 PAN Verification System

A secure, production-ready **PAN Card Verification REST API** built with **Spring Boot 4.1** and **Java 21**, integrating with the **Protean (formerly NSDL) OPV (Online PAN Verification) API** for real-time PAN verification using PKCS7/CMS digital signatures.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Database Schema](#-database-schema)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Default Admin Account](#-default-admin-account)
- [API Endpoints](#-api-endpoints)
- [Request & Response Examples](#-request--response-examples)
- [Protean OPV Integration](#-protean-opv-integration)
- [Digital Signature (PKI)](#-digital-signature-pki)
- [Security](#-security)
- [Reports](#-reports)
- [Logging](#-logging)
- [Project Structure](#-project-structure)
- [Protean Response Codes](#-protean-response-codes)
- [PAN Status Values](#-pan-status-values)
- [Author](#-author)

---

## 📖 Overview

The PAN Verification System provides a secure REST API to verify Indian **PAN (Permanent Account Number)** cards in real-time by integrating with the **Protean OPV API** (Government of India / Income Tax Department). It handles the complete verification lifecycle:

- User registration and JWT-based login
- Digitally signing each verification request with a PKCS7/CMS signature
- Calling the Protean OPV API and interpreting the response
- Persisting all verification records and Protean response metadata to MySQL
- Exposing admin dashboards, stats, and downloadable PDF/Excel reports

---

## ✨ Features

- ✅ Real-time PAN verification via Protean OPV API (UAT & Production)
- ✅ JWT-based authentication (Register / Login)
- ✅ Role-based access control — `ROLE_USER` and `ROLE_ADMIN`
- ✅ Auto-created default admin account on first startup
- ✅ PKCS7/CMS digital signature generation using BouncyCastle (SHA1withRSA)
- ✅ JKS keystore support for DSC (Digital Signature Certificate)
- ✅ Full verification history with search by PAN number
- ✅ Protean response header & output data persisted separately in DB
- ✅ Admin dashboard with total users and verification counts
- ✅ Downloadable PDF and Excel reports of all verifications
- ✅ Audit logging of user actions
- ✅ Comprehensive structured logging via Logback (rolling file appender)
- ✅ Swagger / OpenAPI documentation at `/swagger-ui.html`
- ✅ CORS configuration for frontend integration
- ✅ SSL bypass for Protean UAT (self-signed certificate environment)

---

## 🛠 Tech Stack

| Layer            | Technology                                      |
|------------------|-------------------------------------------------|
| Language         | Java 21                                         |
| Framework        | Spring Boot 4.1                                 |
| Security         | Spring Security, JWT (JJWT 0.12.5)             |
| Database         | MySQL 8.0, Spring Data JPA, Hibernate 7         |
| Cryptography     | BouncyCastle (bcpkix-jdk18on, bcprov-jdk18on)   |
| HTTP Client      | Apache HttpClient5 + Spring RestTemplate        |
| PDF Generation   | iTextPDF 5.5.13.3                               |
| Excel Generation | Apache POI 5.4.1                                |
| JSON Parsing     | Jackson, Gson 2.10.1                            |
| API Docs         | SpringDoc OpenAPI / Swagger UI 2.8.9            |
| Build Tool       | Maven                                           |
| Utilities        | Lombok                                          |

---

## 🏗 Architecture

```
Client (Frontend / Postman / Mobile)
            │
            ▼
  ┌─────────────────────┐
  │  Spring Security     │  ← JWT Auth Filter validates Bearer token
  │  (JwtAuthFilter)     │
  └────────┬────────────┘
           │
    ┌──────▼──────────────────────────────┐
    │         REST Controllers            │
    │  UserController   /api/users/**     │
    │  PanVerification  /api/pan/**       │
    │  ProteanController /api/protean/**  │
    │  AdminController  /api/admin/**     │
    │  ReportController /api/report/**    │
    └──────┬──────────────────────────────┘
           │
    ┌──────▼──────────────────────────────┐
    │           Service Layer             │
    │  PanVerificationService             │
    │    ├─► SignatureService             │  ← PKCS7/CMS signing with JKS
    │    ├─► ProteanService               │  ← HTTP POST to Protean OPV API
    │    └─► PanVerificationRepository    │  ← Persist to MySQL
    │  UserService (register / login)     │
    │  DashboardService                   │
    │  ReportService (PDF / Excel)        │
    │  AuditService                       │
    └──────┬──────────────────────────────┘
           │
    ┌──────▼────────────────┐
    │     MySQL Database     │
    │  pan_verification      │
    │  protean_response_hdr  │
    │  protean_output_data   │
    │  users                 │
    │  audit_log             │
    └───────────────────────┘
```

---

## 🗄 Database Schema

### `users`
| Column       | Type         | Notes                    |
|--------------|--------------|--------------------------|
| id           | BIGINT (PK)  | Auto-increment            |
| username     | VARCHAR(100) | Unique, not null          |
| full_name    | VARCHAR(255) | Not null                  |
| email        | VARCHAR(150) | Unique, not null          |
| password     | VARCHAR(255) | BCrypt hashed             |
| role         | ENUM         | `ROLE_USER` / `ROLE_ADMIN`|
| created_at   | DATETIME     | Auto-set on insert        |
| enabled      | BOOLEAN      | Default true              |

### `pan_verification`
| Column              | Type        | Notes                      |
|---------------------|-------------|----------------------------|
| id                  | BIGINT (PK) | Auto-increment              |
| pan_number          | VARCHAR     | The PAN card number         |
| full_name           | VARCHAR     | Name as entered by user     |
| fathername          | VARCHAR     | Father's name               |
| dob                 | DATE        | Date of birth               |
| pan_status          | VARCHAR     | E / X / D / F / N / EC / ED|
| verification_status | VARCHAR     | `SUCCESS` or `FAILED`       |
| user_id             | BIGINT (FK) | References `users`          |

### `protean_response_header`
| Column              | Type        | Notes                        |
|---------------------|-------------|------------------------------|
| id                  | BIGINT (PK) | Auto-increment                |
| user_id             | VARCHAR     | Protean User ID               |
| records_count       | VARCHAR     | Number of records             |
| response_time       | VARCHAR     | Protean response timestamp    |
| transaction_id      | VARCHAR     | Unique transaction identifier |
| version             | VARCHAR     | API version                   |
| response_code       | VARCHAR     | Protean response code         |
| pan_verification_id | BIGINT (FK) | References `pan_verification` |

### `protean_output_data`
| Column         | Type        | Notes                           |
|----------------|-------------|---------------------------------|
| id             | BIGINT (PK) | Auto-increment                   |
| pan            | VARCHAR     | PAN number from Protean response |
| pan_status     | VARCHAR     | PAN status returned by Protean   |
| name           | VARCHAR     | Name from Protean database       |
| fathername     | VARCHAR     | Father name from Protean         |
| dob            | VARCHAR     | DOB from Protean                 |
| seeding_status | VARCHAR     | Aadhaar seeding status           |
| header_id      | BIGINT (FK) | References `protean_response_header` |

### `audit_log`
| Column     | Type        | Notes                        |
|------------|-------------|------------------------------|
| id         | BIGINT (PK) | Auto-increment                |
| action     | VARCHAR(100)| Action name                   |
| username   | VARCHAR(100)| Who performed the action      |
| ip_address | VARCHAR(50) | Client IP                     |
| details    | TEXT        | Additional context            |
| status     | VARCHAR(10) | SUCCESS / FAILURE             |
| timestamp  | DATETIME    | Auto-set on insert            |

---

## 📋 Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)
- **Protean OPV credentials** — User ID and a registered DSC certificate (`.jks` / `.pfx`)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/mohitpawar61/PanVerification.git
cd PanVerification
```

### 2. Create MySQL Database

```sql
CREATE DATABASE pan_verification_db;
```

### 3. Place Your Certificate

Put your JKS keystore file at:
```
src/main/resources/certs/output.jks
```

If you have a `.pfx` file, convert it to JKS:
```bash
keytool -importkeystore \
  -srckeystore original.pfx \
  -srcstoretype PKCS12 \
  -srcstorepass <pfx_password> \
  -destkeystore src/main/resources/certs/output.jks \
  -deststoretype JKS \
  -deststorepass <jks_password> \
  -destkeypass <jks_password>
```

Verify the keystore loaded correctly:
```bash
keytool -list -keystore src/main/resources/certs/output.jks -storepass <jks_password>
```

### 4. Configure `application.properties`

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=PanVerification

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pan_verification_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# DSC Certificate (JKS keystore)
opv.pfx-file=certs/output.jks
opv.pfx-password=your_jks_password

# Protean OPV API
protean.user-id=YOUR_PROTEAN_USER_ID
protean.version=4
protean.uat-url=https://121.240.36.237/TIN/PanInquiryAPIBackEnd

# JWT Secret (minimum 64 characters for HMAC-SHA256)
jwt.secret=your_64_character_minimum_secret_key_here_xxxxxxxxxxxxxxxxxxxxxxxx

# Logging
logging.level.root=INFO
logging.level.com.verify.panverification=DEBUG
```

### 5. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Or run the packaged JAR:
```bash
mvn clean package
java -jar target/PanVerification-0.0.1-SNAPSHOT.jar
```

The application starts at: **`http://localhost:8080`**

Swagger UI: **`http://localhost:8080/swagger-ui.html`**

---

## 🔧 Configuration

### Key Properties Reference

| Property                  | Description                                     | Example Value                          |
|---------------------------|-------------------------------------------------|----------------------------------------|
| `spring.datasource.url`   | MySQL JDBC connection URL                       | `jdbc:mysql://localhost:3306/pan_db`   |
| `spring.datasource.username` | Database username                            | `root`                                 |
| `spring.datasource.password` | Database password                            | `your_password`                        |
| `opv.pfx-file`            | Path to JKS keystore (classpath relative)       | `certs/output.jks`                     |
| `opv.pfx-password`        | JKS keystore password                           | `your_jks_password`                    |
| `protean.user-id`         | Your Protean-assigned User ID                   | `V0024301`                             |
| `protean.version`         | Protean API version                             | `4`                                    |
| `protean.uat-url`         | Protean UAT endpoint URL                        | `https://121.240.36.237/TIN/...`       |
| `jwt.secret`              | HMAC-SHA256 JWT signing key (64+ chars)         | `1234...` (64 char hex string)         |

---

## 👤 Default Admin Account

On first startup, the application automatically creates a default admin user via `AdminInitializer`:

| Field    | Value             |
|----------|-------------------|
| Email    | `admin@gmail.com` |
| Password | `admin123`        |
| Role     | `ROLE_ADMIN`      |

> ⚠️ **Change the default admin password immediately after first login in any shared or production environment.**

---

## 📡 API Endpoints

### Authentication (Public — No Token Required)

| Method | Endpoint              | Description             |
|--------|-----------------------|-------------------------|
| POST   | `/api/users/register` | Register a new user     |
| POST   | `/api/users/login`    | Login and get JWT token |

### PAN Verification (Authenticated — `ROLE_USER` or `ROLE_ADMIN`)

| Method | Endpoint                    | Description                        |
|--------|-----------------------------|------------------------------------|
| POST   | `/api/pan/verify`           | Verify a PAN card via Protean API  |
| GET    | `/api/pan/history`          | Get all verification records       |
| GET    | `/api/pan/search?pan=XXXXX` | Search verifications by PAN number |
| GET    | `/api/pan/test-cert`        | Test if the JKS certificate loads  |
| POST   | `/api/pan/generate-signature` | Generate PKCS7 signature for JSON  |
| GET    | `/api/pan/test-sign`        | Run a test signature generation    |

### Protean Direct (Authenticated)

| Method | Endpoint                    | Description                           |
|--------|-----------------------------|---------------------------------------|
| POST   | `/api/protean/proteanverify`| Call Protean API directly with PAN   |

### Admin (Authenticated — `ROLE_ADMIN` only)

| Method | Endpoint              | Description                              |
|--------|-----------------------|------------------------------------------|
| GET    | `/api/admin/dashboard`| Get total users and verifications count  |
| GET    | `/api/admin/stats`    | Get user and verification statistics     |

### Reports (Authenticated)

| Method | Endpoint           | Description                                     |
|--------|--------------------|--------------------------------------------------|
| GET    | `/api/report/pdf`  | Download verification history as PDF             |
| GET    | `/api/report/excel`| Download verification history as Excel (.xlsx)  |

### API Documentation

| URL                           | Description              |
|-------------------------------|--------------------------|
| `GET /swagger-ui.html`        | Interactive Swagger UI   |
| `GET /v3/api-docs`            | Raw OpenAPI JSON spec    |

---

## 📝 Request & Response Examples

### Register a User

```http
POST /api/users/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "username": "johndoe",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": null
}
```

---

### Login

```http
POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### Verify a PAN Card

```http
POST /api/pan/verify
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
  "panNumber": "AAAPA0039K",
  "fullName": "rajesh ramdin gupta",
  "fathername": "ramdin",
  "dob": "1989-10-18"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "PAN Verified",
  "data": {
    "panNumber": "AAAPA0039K",
    "panStatus": "E",
    "verificationStatus": "SUCCESS",
    "message": "Success"
  }
}
```

**Response (Failure / Invalid PAN):**
```json
{
  "success": true,
  "message": "PAN Verified",
  "data": {
    "panNumber": "AAAPA0039K",
    "panStatus": null,
    "verificationStatus": "FAILED",
    "message": "Invalid PAN format"
  }
}
```

---

### Get Verification History

```http
GET /api/pan/history
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
[
  {
    "id": 1,
    "panNumber": "AAAPA0039K",
    "fullName": "rajesh ramdin gupta",
    "fathername": "ramdin",
    "dob": "1989-10-18",
    "panStatus": "E",
    "verificationStatus": "SUCCESS"
  }
]
```

---

### Search by PAN Number

```http
GET /api/pan/search?pan=AAAPA
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### Admin Dashboard

```http
GET /api/admin/dashboard
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...   (ADMIN role required)
```

**Response:**
```json
{
  "totalUsers": 5,
  "totalVerifications": 42
}
```

---

### Download PDF Report

```http
GET /api/report/pdf
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Returns a downloadable `pan-report.pdf` file with all verification records.

---

### Download Excel Report

```http
GET /api/report/excel
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Returns a downloadable `pan-report.xlsx` file.

---

## 🔗 Protean OPV Integration

### How It Works — End to End

```
1. User POSTs PAN details to /api/pan/verify
        │
2. DOB is reformatted to DD/MM/YYYY (as required by Protean)
        │
3. Input data is serialized to JSON
        │
4. SignatureService generates a PKCS7/CMS digital signature
   using the DSC certificate from JKS keystore (SHA1withRSA)
        │
5. ProteanService builds the OpvRequest (body + headers):
     Header: User_ID, Records_count, Request_time,
             Transaction_ID (UserID:timestamp), Version
     Body:   inputData array + digital signature
        │
6. HTTP POST to Protean UAT URL
        │
7. Protean returns JSON response with:
     response_Code + outputData array
        │
8. Response parsed → protean_response_header and
   protean_output_data saved to MySQL
        │
9. pan_verification record updated with status
        │
10. API returns PAN status and verification result to client
```

### Request Headers Sent to Protean

| Header          | Value                           |
|-----------------|---------------------------------|
| `User_ID`       | Your Protean-assigned User ID   |
| `Records_count` | Number of PAN records (1–5)     |
| `Request_time`  | `yyyy-MM-dd'T'HH:mm:ss`         |
| `Transaction_ID`| `{UserID}:{System.currentTimeMillis()}` |
| `Version`       | `4`                             |
| `Content-Type`  | `application/json`              |

### Direct Protean Endpoint

You can also call Protean directly (bypassing the full save flow) via:

```http
POST /api/protean/proteanverify
Authorization: Bearer <token>
Content-Type: application/json

{
  "pan": "AAAPA0039K",
  "name": "rajesh ramdin gupta",
  "fathername": "ramdin",
  "dob": "18/10/1989"
}
```

> Note: The `dob` format here must be `DD/MM/YYYY` when calling the Protean endpoint directly.

---

## 🔐 Digital Signature (PKI)

The application generates **PKCS7/CMS digital signatures** as required by the Protean OPV API specification.

### Certificate Setup

- The signature uses a **JKS keystore** (`output.jks`) containing your Protean-registered DSC (Digital Signature Certificate).
- Algorithm: **SHA1withRSA** with **BouncyCastle** as the security provider.
- The generated signature is **Base64-encoded** and sent as part of the OPV request body.

### Test Endpoints

| Endpoint                       | Purpose                                          |
|--------------------------------|--------------------------------------------------|
| `GET /api/pan/test-cert`       | Verifies the JKS keystore loads and prints alias |
| `GET /api/pan/test-sign`       | Runs a test signature and returns Base64 output  |
| `POST /api/pan/generate-signature` | Signs a custom JSON string you provide      |

### Converting PFX to JKS

If Protean provided you a `.pfx` certificate:

```bash
keytool -importkeystore \
  -srckeystore VerasysSubCA2022.pfx \
  -srcstoretype PKCS12 \
  -srcstorepass <pfx_password> \
  -destkeystore src/main/resources/certs/output.jks \
  -deststoretype JKS \
  -deststorepass abc1234 \
  -destkeypass abc1234
```

---

## 🔒 Security

- All endpoints except `/api/users/register`, `/api/users/login`, and `/swagger-ui/**` require a valid **JWT Bearer token**.
- JWT tokens are signed with **HMAC-SHA256** using a configurable 64+ character secret.
- Passwords are hashed with **BCrypt**.
- Role-based authorization:
  - `ROLE_ADMIN` — required for `/api/admin/**`
  - `ROLE_USER` or `ROLE_ADMIN` — required for `/api/user/**`
  - All other authenticated endpoints — require any valid role
- CORS is configured for cross-origin frontend integration.
- SSL certificate verification is **bypassed** for Protean UAT (self-signed certificate at `121.240.36.237`).

> ⚠️ **Production Note:** Remove the SSL bypass in `RestTemplateConfig` and configure proper trust stores for production environments.

---

## 📊 Reports

The `ReportController` exposes two downloadable report formats:

| Format  | Endpoint           | File Name        | Library     |
|---------|--------------------|------------------|-------------|
| PDF     | `GET /api/report/pdf`  | `pan-report.pdf` | iTextPDF    |
| Excel   | `GET /api/report/excel`| `pan-report.xlsx`| Apache POI  |

Both reports contain the complete PAN verification history stored in the database. JWT authentication is required.

---

## 📋 Logging

The project uses **Logback** with a rolling file appender configured in `logback-spring.xml`.

Log files are stored in the `logs/` directory:

| File                                | Description                    |
|-------------------------------------|--------------------------------|
| `logs/pan-verification.log`         | Current application log        |
| `logs/pan-verification.YYYY-MM-DD.log` | Daily rolled log archive    |

### Log Level Configuration

```properties
logging.level.root=INFO
logging.level.com.verify.panverification=DEBUG
```

Key events logged:
- Every API request/response (PAN number, status)
- Full Protean request and response headers
- Protean response body (PAN status, name, DOB, seeding status)
- Certificate load and signature generation
- User registration and login events
- Database persistence operations

---

## 📁 Project Structure

```
PanVerification/
├── pom.xml
├── logs/
│   ├── pan-verification.log
│   └── pan-verification.YYYY-MM-DD.log
└── src/
    ├── main/
    │   ├── java/com/verify/panverification/
    │   │   ├── PanVerificationApplication.java      # Main entry point
    │   │   ├── config/
    │   │   │   ├── AdminInitializer.java            # Auto-creates default admin on startup
    │   │   │   ├── AppConfig.java                   # General app config (BCrypt bean, etc.)
    │   │   │   ├── CorsConfig.java                  # CORS configuration
    │   │   │   ├── RestTemplateConfig.java          # HTTP client with SSL bypass for UAT
    │   │   │   └── SwaggerConfig.java               # OpenAPI / Swagger UI setup
    │   │   ├── controller/
    │   │   │   ├── AdminController.java             # /api/admin/dashboard, /stats
    │   │   │   ├── PanVerificationController.java   # /api/pan/**
    │   │   │   ├── ProteanController.java           # /api/protean/proteanverify
    │   │   │   ├── ReportController.java            # /api/report/pdf, /excel
    │   │   │   └── UserController.java              # /api/users/register, /login
    │   │   ├── dto/
    │   │   │   ├── ApiResponse.java                 # Generic API response wrapper
    │   │   │   ├── DashboardResponse.java           # Admin dashboard stats
    │   │   │   ├── InputDataDto.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── LoginResponse.java               # Contains JWT token
    │   │   │   ├── OpvRequest.java                  # Full Protean OPV request body
    │   │   │   ├── PanRequest.java                  # Single PAN entry for Protean
    │   │   │   ├── PanVerificationRequest.java      # User-facing verify request DTO
    │   │   │   ├── PanVerificationResponse.java     # User-facing verify response DTO
    │   │   │   ├── RegisterRequest.java
    │   │   │   └── RequestBody.java
    │   │   ├── entity/
    │   │   │   ├── AuditLog.java                    # Audit trail entity
    │   │   │   ├── PanVerification.java             # PAN verification record
    │   │   │   ├── ProteanOutputData.java           # Protean outputData rows
    │   │   │   ├── ProteanResponseHeader.java       # Protean response headers
    │   │   │   ├── Role.java                        # ROLE_USER / ROLE_ADMIN enum
    │   │   │   └── User.java                        # Registered user (implements UserDetails)
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java      # @ControllerAdvice for all exceptions
    │   │   │   ├── InvalidPanException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── UserAlreadyExistsException.java
    │   │   ├── repository/
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── PanVerificationRepository.java
    │   │   │   ├── ProteanOutputDataRepository.java
    │   │   │   ├── ProteanResponseHeaderRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── CustomUserDetailsService.java    # Loads user from DB for Spring Security
    │   │   │   ├── JwtAuthFilter.java               # Validates JWT on every request
    │   │   │   ├── JwtService.java                  # Token generation and validation
    │   │   │   └── SecurityConfig.java              # Security filter chain + route permissions
    │   │   ├── service/
    │   │   │   ├── AuditService.java                # Saves audit log entries
    │   │   │   ├── CertificateService.java          # Certificate utility operations
    │   │   │   ├── DashboardService.java            # Admin dashboard aggregation
    │   │   │   ├── PanVerificationService.java      # Core verification orchestration
    │   │   │   ├── ProteanService.java              # Protean API HTTP integration
    │   │   │   ├── ReportService.java               # PDF and Excel report generation
    │   │   │   ├── SignatureService.java            # PKCS7/CMS digital signature
    │   │   │   └── UserService.java                 # Register, login, JWT issuance
    │   │   └── util/
    │   │       ├── Constants.java
    │   │       ├── ExcelGenerator.java              # Apache POI Excel builder
    │   │       └── PdfGenerator.java               # iTextPDF builder
    │   └── resources/
    │       ├── application.properties
    │       ├── logback-spring.xml                   # Logging configuration
    │       └── certs/
    │           ├── output.jks                       # JKS keystore (DSC certificate)
    │           └── VerasysSubCA2022.pfx             # Original PFX (source certificate)
    └── test/
        └── java/com/verify/panverification/
            └── PanVerificationApplicationTests.java
```

---

## 📊 Protean Response Codes

| Code | Meaning                                           |
|------|---------------------------------------------------|
| 1    | Success                                           |
| 2    | System Error                                      |
| 3    | Authentication Failure — Check certificate/User ID|
| 4    | User not authorized                               |
| 5    | No PANs entered or request limit exceeded         |
| 6    | User validity expired                             |
| 8    | Not enough balance                                |
| 12   | Invalid version number                            |
| 18   | Wrong User ID or Certificate                      |
| 19   | Digital signature missing                         |
| 20   | Request body is blank                             |
| 22   | Invalid PAN format                                |
| 23   | System Failure                                    |
| 24   | Duplicate Transaction ID                          |
| 25   | JSON Parse Exception                              |
| 26   | Records Count mismatch                            |
| 27   | Invalid Name                                      |
| 28   | Invalid Father Name                               |
| 29   | Invalid DOB format                                |
| 30   | Invalid Request Time                              |
| 31   | Invalid Transaction ID                            |
| 32   | Invalid Record Count                              |
| 33   | Request Time not within last 30 minutes           |

---

## 🪪 PAN Status Values

| Status | Meaning                                       |
|--------|-----------------------------------------------|
| `E`    | Existing and Valid                            |
| `EC`   | Valid — Acquisition (consolidation) event     |
| `ED`   | Valid — Death event                           |
| `X`    | Deactivated                                   |
| `D`    | Deleted                                       |
| `F`    | Fake                                          |
| `N`    | Not found in Income Tax Department database   |

---

## 👨‍💻 Author

**Mohit Pawar**
- GitHub: [@mohitpawar61](https://github.com/mohitpawar61)

---

## 📄 License

This project is built for integration with the Protean OPV API and is intended for educational and enterprise KYC/PAN verification purposes.
