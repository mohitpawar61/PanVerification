# 🔍 PAN Verification System

A secure, production-ready **PAN Card Verification API** built with **Spring Boot**, integrating with **Protean (NSDL) OPV API** for real-time PAN verification using digital signatures.

---

## 📌 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Protean OPV Integration](#protean-opv-integration)
- [Security](#security)
- [Project Structure](#project-structure)
- [Response Codes](#response-codes)

---

## 📖 Overview

The PAN Verification System provides a REST API to verify Indian PAN (Permanent Account Number) cards in real-time using the **Protean (formerly NSDL) OPV (Online PAN Verification) API**. It supports user authentication via JWT, digital signature generation using PKCS7/CMS, and maintains a full verification history in a MySQL database.

---

## ✨ Features

- ✅ Real-time PAN verification via Protean OPV API
- ✅ JWT-based authentication (Register / Login)
- ✅ Role-based access control (USER / ADMIN)
- ✅ Digital signature generation using PKCS7/CMS with BouncyCastle
- ✅ SSL bypass for UAT environment
- ✅ Verification history tracking
- ✅ PAN search functionality
- ✅ Swagger/OpenAPI documentation
- ✅ Admin dashboard support
- ✅ Comprehensive logging

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1 |
| Security | Spring Security, JWT (JJWT 0.12.5) |
| Database | MySQL 8.0, Spring Data JPA, Hibernate 7 |
| Signature | BouncyCastle (bcpkix, bcprov) |
| HTTP Client | Apache HttpClient5 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| IDE | IntelliJ IDEA |

---

## 🏗 Architecture

```
Client (Frontend / Postman)
        │
        ▼
Spring Security (JWT Filter)
        │
        ▼
PAN Verification Controller
        │
        ▼
PAN Verification Service
        │
        ├──► Signature Service (PKCS7/CMS via BouncyCastle)
        │
        ├──► Protean Service (HTTP POST to OPV API)
        │         │
        │         ▼
        │    Protean UAT API
        │    https://121.240.36.237/TIN/PanInquiryAPIBackEnd
        │
        └──► PAN Verification Repository (MySQL)
```

---

## 📋 Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Protean OPV API credentials (User ID, DSC certificate `.jks`)

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

### 3. Place Certificate File

Put your JKS keystore file at:
```
src/main/resources/certs/output.jks
```

### 4. Configure `application.properties`

```properties
spring.application.name=PanVerification

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pan_verification_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Certificate
opv.pfx-file=certs/output.jks
opv.pfx-password=your_jks_password

# Protean OPV API
protean.user-id=YOUR_USER_ID
protean.version=4
protean.uat-url=https://121.240.36.237/TIN/PanInquiryAPIBackEnd

# JWT
jwt.secret=your_64_character_secret_key

# Logging
logging.level.root=INFO
logging.level.com.verify.panverification=DEBUG
```

### 5. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

Application starts at: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🔧 Configuration

### Certificate Setup

The system uses a **JKS keystore** containing your Protean-registered DSC certificate for generating PKCS7/CMS digital signatures.

To verify your JKS file and password:
```bash
keytool -list -keystore src/main/resources/certs/output.jks -storepass your_password
```

To convert a PFX to JKS:
```bash
keytool -importkeystore \
  -srckeystore original.pfx \
  -srcstoretype PKCS12 \
  -srcstorepass pfx_password \
  -destkeystore certs/output.jks \
  -deststoretype JKS \
  -deststorepass your_password \
  -destkeypass your_password
```

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/users/register` | Register new user | No |
| POST | `/api/users/login` | Login and get JWT token | No |

### PAN Verification

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/pan/verify` | Verify a PAN card | Yes (USER) |
| GET | `/api/pan/history` | Get verification history | Yes (USER) |
| GET | `/api/pan/search?pan=XXXXX` | Search by PAN number | Yes (USER) |

### Admin

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/admin/users` | Get all users | Yes (ADMIN) |

---

### Request / Response Examples

#### Register
```json
POST /api/users/register
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "username": "johndoe",
  "password": "password123"
}
```

#### Login
```json
POST /api/users/login
{
  "email": "john@example.com",
  "password": "password123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Verify PAN
```json
POST /api/pan/verify
Authorization: Bearer <token>

{
  "panNumber": "AAAPA0039K",
  "fullName": "rajesh ramdin gupta",
  "fathername": "ramdin",
  "dob": "1989-10-18"
}
```
Response:
```json
{
  "panNumber": "AAAPA0039K",
  "panStatus": "E",
  "verificationStatus": "SUCCESS",
  "message": "Success"
}
```

---

## 🔐 Protean OPV Integration

### How It Works

1. User submits PAN details via REST API
2. DOB is formatted to `DD/MM/YYYY` as required by Protean
3. Input JSON is signed using **PKCS7/CMS digital signature** (SHA1withRSA + BouncyCastle)
4. Request is sent to Protean UAT API with required headers
5. Response is parsed and saved to database

### Request Headers Sent to Protean

| Header | Value |
|---|---|
| `User_ID` | Your Protean User ID |
| `Records_count` | Number of PANs (1-5) |
| `Request_time` | `yyyy-MM-dd'T'HH:mm:ss` |
| `Transaction_ID` | `UserID:timestamp` |
| `Version` | `4` |

### PAN Status Values

| Status | Meaning |
|---|---|
| `E` | Existing and Valid |
| `X` | Deactivated |
| `D` | Deleted |
| `F` | Fake |
| `N` | Not found in ITD database |
| `EC` | Valid — Acquisition event |
| `ED` | Valid — Death event |

---

## 🔒 Security

- All endpoints (except register/login) require a valid **JWT Bearer token**
- JWT tokens are signed with HMAC-SHA256
- Passwords are encrypted with **BCrypt**
- SSL certificate verification is bypassed for UAT (self-signed cert on `121.240.36.237`)
- **Note:** For production, remove SSL bypass and use proper certificates

---

## 📁 Project Structure

```
src/main/java/com/verify/panverification/
├── config/
│   ├── AdminInitializer.java       # Creates default admin on startup
│   ├── CorsConfig.java             # CORS configuration
│   ├── RestTemplateConfig.java     # HTTP client with SSL bypass
│   └── SwaggerConfig.java          # OpenAPI/Swagger config
├── controller/
│   ├── PanVerificationController.java
│   ├── ProteanController.java
│   └── UserController.java
├── dto/
│   ├── PanRequest.java             # Input to Protean API
│   ├── PanVerificationRequest.java # User request DTO
│   ├── PanVerificationResponse.java
│   └── OpvRequest.java             # Full Protean request body
├── entity/
│   ├── PanVerification.java        # Verification record entity
│   └── User.java
├── repository/
│   ├── PanVerificationRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
└── service/
    ├── PanVerificationService.java  # Main verification logic
    ├── ProteanService.java          # Protean API caller
    ├── SignatureService.java        # PKCS7/CMS signature generator
    └── UserService.java

src/main/resources/
├── certs/
│   └── output.jks                  # DSC certificate keystore
└── application.properties
```

---

## 📊 Response Codes

| Code | Meaning |
|---|---|
| 1 | Success |
| 2 | System Error |
| 3 | Authentication Failure |
| 4 | User not authorized |
| 5 | No PANs entered or limit exceeded |
| 6 | User validity expired |
| 8 | Not enough balance |
| 18 | Wrong User ID or Certificate |
| 19 | Digital signature missing |
| 22 | Invalid PAN format |
| 24 | Duplicate Transaction ID |
| 29 | Invalid DOB format |
| 33 | Request Time not within last 30 minutes |

---

## 👨‍💻 Author

**Mohit Pawar**
- GitHub: [@mohitpawar61](https://github.com/mohitpawar61)

---

## 📄 License

This project is for educational and integration purposes with Protean OPV API.
