# 📱 SmsMonitor (SMS 기반 소비 및 자산 관리 서비스)

사용자의 SMS(카드 결제 문자)를 실시간으로 수신/파싱하여 **지출 내역을 자동으로 기록하고, 자산과 연동하여 소비 습관 및 멍청비용을 분석**해주는 백엔드 API 서비스입니다.

---

## 🛠 기술 스택

| 분류 | 기술 스택 |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.4.5 |
| **Security** | Spring Security, JWT (Access / Refresh Token) |
| **Persistence** | Spring Data JPA, H2 Database (Local), MySQL (Prod) |
| **Push Notification** | Firebase Admin SDK (FCM) |
| **API Documentation** | SpringDoc OpenAPI 3 (Swagger UI), Spring RestDocs, Asciidoctor |
| **Build & Test** | Gradle, JUnit 5, Mockito |

---

## 🌟 핵심 기능

### 1. 사용자 및 디바이스 관리 (User & Device)
- **JWT 기반 인증/인가**: Access Token(단기) 및 HttpOnly Cookie 기반 Refresh Token(장기) 발급/갱신.
- **관리자 승인 시스템**: 승인(`isApproved`)된 사용자만 서비스 접근 가능.
- **FCM 디바이스 토큰 연동**: 결제 문자 수신 시 등록된 기기들로 실시간 푸시 알림 발송.

### 2. 거래 내역 및 소비 분석 (Transaction)
- **SMS 자동 파싱 (`SmartSmsParser`)**: 다양한 금융사 결제 문자 포맷을 자동 인식하여 지출 내역으로 저장.
- **중복 수신 방지 (Deduplication)**: 짧은 시간 내 동일한 문자가 재전송될 경우 중복 처리 방지.
- **멍청비용(Stupid Cost) 분석**: 고액 결제, 심야 시간대 결제, 편의점 지출 등 전략 패턴 기반 낭비성 지출 자동 감지.
- **통계 및 요약**:
  - 월간 수입/지출/멍청비용 요약 (`/summary`)
  - 일간/주간/월간/연간 주기별 소비 통계 차트 데이터 (`/statistics`)
  - 목표 저축액 대비 가이드라인 분석 (`/analysis/savings`)

### 3. 계좌 및 자산 연동 (Account)
- **실시간 자산 동기화**: 결제 내역 등록/수정/삭제 시 `reflectInAsset` 설정에 따라 기본 계좌 잔액 자동 가감.
- **다중 계좌 관리**: 은행별 계좌 등록, 잔액 관리 및 기본 계좌 설정.

### 4. 소비 카테고리 관리 (Category)
- 지출 카테고리 CRUD 및 멍청비용 대상 카테고리 커스텀 지정.

---

## 🏗 프로젝트 구조 (DDD - Domain Driven Design)

```
src/main/java/com/mk/www/smsmonitor
├── account          # 계좌 및 자산 도메인 (계좌 등록, 잔액 관리)
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── category         # 지출 카테고리 도메인
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── common           # 공통 모듈 (Security, JWT, FCM, Global Exception, ApiResponse)
│   ├── api
│   ├── application
│   ├── config
│   └── util
├── transaction      # 거래 내역 도메인 (SMS 파싱, 지출 통계, 멍청비용 분석)
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
└── user             # 사용자 및 디바이스 도메인 (인증, 토큰, 회원 관리)
    ├── api
    ├── application
    ├── domain
    └── infrastructure
```

---

## 🚀 실행 방법

### 1. 사전 요구사항
- JDK 17 이상

### 2. 로컬 환경 실행 (H2 In-Memory DB)
별도의 외부 데이터베이스 설치 없이 바로 실행 가능합니다. 기본적으로 `local` 프로파일이 활성화되어 H2 메모리 DB가 동작합니다.

```bash
# 저장소 복제
git clone https://github.com/Mkw-k/SmsMonitor.git
cd SmsMonitor-2

# 애플리케이션 실행
./gradlew bootRun
```

- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:smsmonitor`
  - **User**: `sa`
  - **Password**: *(공백)*
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 3. 운영 환경 실행 (`prod` 프로파일)
운영 환경에서는 MySQL 데이터베이스를 사용합니다.

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:mysql://<DB_HOST>:3306/<DB_NAME> \
DB_USERNAME=<DB_USER> \
DB_PASSWORD=<DB_PASSWORD> \
./gradlew bootRun
```

---

## 📑 주요 API 명세

| 도메인 | 메서드 | 엔드포인트 | 설명 |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | 회원가입 |
| | `POST` | `/api/auth/login` | 로그인 및 JWT 토큰 발급 |
| | `POST` | `/api/auth/refresh` | 리프레시 토큰으로 액세스 토큰 재발급 |
| **Device** | `POST` | `/api/devices/register` | FCM 디바이스 토큰 등록 |
| | `GET` | `/api/devices` | 등록된 디바이스 목록 조회 |
| **Transaction** | `POST` | `/api/transactions/sms` | SMS 수신 및 자동 파싱/등록 (푸시 발송) |
| | `POST` | `/api/transactions` | 거래내역 수동 등록 |
| | `GET` | `/api/transactions` | 거래내역 조회 (페이징 & 필터) |
| | `PUT` | `/api/transactions/{id}` | 거래내역 수정 (자산 자동 연동) |
| | `PUT` | `/api/transactions/{id}/memo`| 거래내역 메모 수정 |
| | `GET` | `/api/transactions/summary` | 이번 달 지출 요약 |
| | `GET` | `/api/transactions/statistics` | 기간별 지출 통계 |
| | `GET` | `/api/transactions/analysis/savings`| 목표 저축액 기반 소비 분석 |
| **Account** | `POST` | `/api/accounts` | 신규 계좌 등록 |
| | `GET` | `/api/accounts` | 계좌 목록 조회 |
| | `PATCH`| `/api/accounts/{id}/default` | 기본 계좌 설정 |
| **Category** | `GET` | `/api/spending-categories` | 소비 카테고리 목록 조회 |
| | `POST` | `/api/spending-categories` | 신규 카테고리 등록 |

---

## 🧪 테스트 실행

```bash
./gradlew test
```
- RestDocs API 명세 및 단위/통합 테스트 자동 실행.

---

## 실행화면

https://github.com/user-attachments/assets/f2c200b7-c3d5-42eb-9c21-4d58789ed115

https://github.com/user-attachments/assets/b5cd6aed-6d81-4b72-8cbe-c3736ed01351

https://github.com/user-attachments/assets/af9049fc-7078-442e-860b-79a0835f4cce

https://github.com/user-attachments/assets/4af5de37-d3f3-4ea2-8015-0c6c34df5915

https://github.com/user-attachments/assets/8b52f08a-dde3-4c5f-b806-25e4fa5e5ff3

https://github.com/user-attachments/assets/7fbd4afd-bcd3-464a-a2c0-32a87d2366b5
