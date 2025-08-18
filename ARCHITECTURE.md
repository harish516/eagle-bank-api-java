# Eagle Bank API - Architecture Documentation

## 🏛️ Overview

The Eagle Bank API is a modern, cloud-native banking application built with Spring Boot 3.2.0 and Java 21. It follows microservices principles, implements enterprise-grade security, and provides comprehensive scalability with resilience patterns. The application is designed to handle high-throughput banking operations with sub-100ms response times and 99.9% availability.

## 📱 Application Architecture

### **Core Framework & Technology Stack**

#### **Runtime & Framework**
- **Java 21** - Latest LTS with modern language features and virtual threads
- **Spring Boot 3.2.0** - Enterprise application framework with auto-configuration
- **Spring Security** - Comprehensive security framework with OAuth2 support
- **Spring Data JPA** - Data access layer with Hibernate ORM
- **Spring Web MVC** - RESTful API framework with reactive capabilities

#### **Security & Authentication**
- **Keycloak 23.0** - OAuth2/OpenID Connect authentication server
- **JWT (JSON Web Tokens)** - Stateless authentication with RS256 signing
- **Spring Security OAuth2 Resource Server** - JWT validation and authorization
- **Custom Security Handlers** - Enhanced authentication and access denied handling

#### **Database & Persistence**
- **PostgreSQL** - Production database with ACID compliance
- **H2 Database** - In-memory database for testing and development
- **Spring Data JPA** - Repository pattern with custom query methods
- **Hibernate** - JPA implementation with event listeners for auditing
- **Database Migration** - Flyway/Liquibase for schema versioning

#### **Monitoring & Observability**
- **Spring Boot Actuator** - Health checks and metrics endpoints
- **Micrometer with Prometheus** - Application metrics collection and export
- **Logback with Structured Logging** - JSON-formatted logging for analysis
- **Custom Performance Monitoring** - AOP-based execution time tracking
- **Audit Service** - Comprehensive security event logging

#### **Resilience & Fault Tolerance**
- **Resilience4j** - Circuit breaker, retry, bulkhead, and time limiter patterns
- **Bucket4j with Caffeine** - Token bucket rate limiting implementation
- **Custom Exception Handling** - Global exception management with proper HTTP status codes

### **Layered Architecture Pattern**

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                       │
├─────────────────────────────────────────────────────────────────┤
│  Controllers (REST Endpoints)                                  │
│  ├── UserController           ├── BankAccountController        │
│  ├── TransactionController    ├── BaseController (Common)      │
│  └── Exception Handlers       └── Security Interceptors       │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Service Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  Business Logic Services                                       │
│  ├── UserService              ├── BankAccountService           │
│  ├── TransactionService       ├── AuditService                 │
│  ├── Performance Monitoring   ├── Rate Limiting Service        │
│  └── Authentication Utils     └── Exception Translation        │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Repository Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  Data Access Objects (Spring Data JPA)                         │
│  ├── UserRepository           ├── BankAccountRepository        │
│  ├── TransactionRepository    ├── Custom Query Methods         │
│  └── JPA Event Listeners      └── Database Auditing            │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                             │
├─────────────────────────────────────────────────────────────────┤
│  ├── PostgreSQL (Primary)     ├── H2 (Testing)                │
│  ├── JPA Entities with        ├── Audit Tables                │
│  │   Validation               ├── Performance Indexes         │
│  └── Database Event Logging   └── Schema Migrations           │
└─────────────────────────────────────────────────────────────────┘
```

### **Domain Model Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Domain   │    │ Account Domain  │    │Transaction Domain│
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • User Entity   │───▶│ • BankAccount   │───▶│ • Transaction   │
│ • Address VO    │    │ • Balance       │    │ • TransferLog   │
│ • Email/Phone   │    │ • AccountType   │    │ • AuditTrail    │
│ • Audit Fields  │    │ • Currency      │    │ • Type/Status   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Security Domain │    │  Audit Domain   │    │ System Domain   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • JWT Tokens    │    │ • Security Event│    │ • Configuration │
│ • Permissions   │    │ • Audit Log     │    │ • Health Checks │
│ • Rate Limits   │    │ • Compliance    │    │ • Performance   │
│ • Base Controller│    │ • Event Types   │    │ • Monitoring    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔒 Security Architecture

### **Multi-Layer Security Model**

```
Internet Traffic
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Layer 1: Network Security                   │
├─────────────────────────────────────────────────────────────────┤
│ • Docker Network Isolation                                     │
│ • Rate Limiting (100 req/min default, configurable)           │
│ • DDoS Protection via Ingress Controller                       │
│ • CORS Configuration for API access                            │
└─────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Layer 2: Application Gateway                  │
├─────────────────────────────────────────────────────────────────┤
│ • SSL/TLS Termination (in production)                          │
│ • Load Balancing across service instances                      │
│ • Health Checks (/actuator/health)                             │
│ • Request Routing and Path Mapping                             │
└─────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                Layer 3: Authentication & Authorization         │
├─────────────────────────────────────────────────────────────────┤
│ • Keycloak OAuth2/OpenID Connect Integration                   │
│ • JWT Token Validation (RS256 signature)                      │
│ • Role-Based Access Control (RBAC)                            │
│ • Custom Authentication Entry Point                            │
└─────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Layer 4: Application Security                │
├─────────────────────────────────────────────────────────────────┤
│ • Input Validation (Bean Validation + Custom)                  │
│ • SQL Injection Prevention (JPA Parameterized Queries)        │
│ • XSS Protection via Content Security Policy                   │
│ • CSRF Protection (Stateless API)                              │
└─────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Layer 5: Data Security                      │
├─────────────────────────────────────────────────────────────────┤
│ • Data Encryption at Rest (Database level)                     │
│ • Database Access Controls (User/Schema separation)            │
│ • Comprehensive Audit Logging                                  │
│ • Sensitive Data Masking in Logs                               │
└─────────────────────────────────────────────────────────────────┘
```

### **Authentication & Authorization Flow**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │ Rate Limit  │    │  Keycloak   │    │Eagle Bank API│
│ Application │    │   Filter    │    │   Server    │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │ 1. API Request    │                   │                   │
       ├──────────────────▶│                   │                   │
       │                   │ 2. Rate Check     │                   │
       │                   │   (Token Bucket)  │                   │
       │                   │                   │                   │
       │                   │ 3. JWT Extract    │                   │
       │                   │   & Validate      │                   │
       │                   ├──────────────────▶│                   │
       │                   │ 4. Token Valid    │                   │
       │                   │◀──────────────────┤                   │
       │                   │ 5. Authorized     │                   │
       │                   │   Request         │                   │
       │                   ├─────────────────────────────────────▶│
       │                   │                   │ 6. User Context   │
       │                   │                   │   Extraction      │
       │                   │                   │ 7. Resource       │
       │                   │                   │   Authorization   │
       │                   │                   │ 8. API Response   │
       │                   │◀─────────────────────────────────────┤
       │ 9. Final Response │                   │                   │
       │◀──────────────────┤                   │                   │
```

### **Security Components**

#### **Rate Limiting Architecture**
```
┌─────────────────────────────────────────────────────────────────┐
│                     Rate Limiting System                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Token Bucket Algorithm (Bucket4j + Caffeine)                  │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ Default Policy  │    │ Strict Policy   │    │ Relaxed     │ │
│  │                 │    │                 │    │ Policy      │ │
│  │ • 100 req/min   │    │ • 20 req/min    │    │ • 200 req/min│ │
│  │ • Burst: 20     │    │ • Burst: 5      │    │ • Burst: 50 │ │
│  │ • General APIs  │    │ • Auth endpoints│    │ • Read ops  │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│                                                                 │
│  Cache Storage (Caffeine)                                      │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ • User-based rate limiting                                 │ │
│  │ • In-memory cache for fast access                          │ │
│  │ • TTL: 1 hour per bucket                                   │ │
│  │ • Maximum 10,000 cached buckets                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 💾 Data Architecture

### **Database Design & Relationships**

#### **Entity Relationship Diagram**
```
┌─────────────────────┐
│       Users         │
│  ┌─────────────────┐│
│  │ • id (PK)       ││ 1
│  │ • name          ││ │
│  │ • email (Unique)││ │
│  │ • address (VO)  ││ │
│  │ • phoneNumber   ││ │
│  │ • timestamps    ││ │
│  └─────────────────┘│ │
└─────────────────────┘ │ 1:N
                        │
                        ▼ N
┌─────────────────────┐
│    Bank Accounts    │
│  ┌─────────────────┐│
│  │ • id (PK)       ││ 1
│  │ • accountNumber ││ │
│  │ • accountName   ││ │
│  │ • balance       ││ │
│  │ • accountType   ││ │
│  │ • currency      ││ │
│  │ • user_id (FK)  ││ │
│  │ • timestamps    ││ │
│  └─────────────────┘│ │
└─────────────────────┘ │ 1:N
                        │
                        ▼ N
┌─────────────────────┐
│    Transactions     │
│  ┌─────────────────┐│
│  │ • id (PK)       ││
│  │ • amount        ││
│  │ • description   ││
│  │ • type (ENUM)   ││
│  │ • account_id(FK)││
│  │ • timestamp     ││
│  └─────────────────┘│
└─────────────────────┘
```

#### **Database Architecture**
```
┌─────────────────────────────────────────────────────────────────┐
│                        PostgreSQL Cluster                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   Primary DB    │    │   H2 Database   │    │ Development │ │
│  │ (Production)    │    │   (Testing)     │    │ Environment │ │
│  │                 │    │                 │    │             │ │
│  │ • Users         │    │ • In-Memory     │    │ • H2 Console│ │
│  │ • Bank Accounts │    │ • Test Data     │    │ • Fast Setup│ │
│  │ • Transactions  │    │ • Isolated      │    │ • Auto DDL  │ │
│  │ • Audit Logs    │    │ • No Persistence│    │ • Debug SQL │ │
│  │ • Performance   │    │                 │    │             │ │
│  │   Optimized     │    │                 │    │             │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                      │       │
│           ▼                       ▼                      ▼       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 JPA Configuration                          │ │
│  │                                                             │ │
│  │ • Hibernate as JPA Provider                                │ │
│  │ • Automatic DDL in dev/test (create-drop)                  │ │
│  │ • Manual schema management in production                   │ │
│  │ • Optimized connection pooling                             │ │
│  │ • Second-level cache for frequently accessed data          │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Data Flow Architecture**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │ Controller  │    │ Service     │    │ Repository  │
│ Application │    │ Layer       │    │ Layer       │    │ Layer       │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │ 1. API Request    │                   │                   │
       │   (JSON)          │                   │                   │
       ├──────────────────▶│                   │                   │
       │                   │ 2. DTO Validation │                   │
       │                   │   & Mapping       │                   │
       │                   │                   │                   │
       │                   │ 3. Business Call  │                   │
       │                   ├──────────────────▶│                   │
       │                   │                   │ 4. Transaction    │
       │                   │                   │   Management      │
       │                   │                   │                   │
       │                   │                   │ 5. Data Access    │
       │                   │                   ├──────────────────▶│
       │                   │                   │                   │ 6. JPA Query
       │                   │                   │                   │    Execution
       │                   │                   │                   │ ┌─────────────┐
       │                   │                   │                   │ │  Database   │
       │                   │                   │                   ├▶│ PostgreSQL  │
       │                   │                   │                   │ │     or      │
       │                   │                   │                   │ │     H2      │
       │                   │                   │                   │ └─────────────┘
       │                   │                   │ 7. Entity Result  │
       │                   │                   │◀──────────────────┤
       │                   │ 8. DTO Mapping    │                   │
       │                   │   & Response      │                   │
       │                   │◀──────────────────┤                   │
       │ 9. JSON Response  │                   │                   │
       │◀──────────────────┤                   │                   │
```

### **Audit & Event Logging Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                     Audit Logging System                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Database Event Listeners (Hibernate)                          │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ Post Insert     │    │ Post Update     │    │ Post Delete │ │
│  │ Listener        │    │ Listener        │    │ Listener    │ │
│  │                 │    │                 │    │             │ │
│  │ • Entity Name   │    │ • Entity Name   │    │ • Entity ID │ │
│  │ • New ID        │    │ • Entity ID     │    │ • Operation │ │
│  │ • Timestamp     │    │ • Changes       │    │ • Timestamp │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│                                                                 │
│           │                      │                      │       │
│           ▼                      ▼                      ▼       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 Security Audit Service                     │ │
│  │                                                             │ │
│  │ Event Types Tracked:                                       │ │
│  │ • AUTHENTICATION_SUCCESS/FAILURE                           │ │
│  │ • AUTHORIZATION_SUCCESS/FAILURE                            │ │
│  │ • RATE_LIMIT_EXCEEDED                                      │ │
│  │ • FINANCIAL_TRANSACTION                                    │ │
│  │ • USER_MANAGEMENT                                          │ │
│  │ • ACCOUNT_DELETION                                         │ │
│  │ • SUSPICIOUS_ACTIVITY                                      │ │
│  │                                                             │ │
│  │ Log Format: Structured JSON with:                          │ │
│  │ • Timestamp, Event Type, User ID                           │ │
│  │ • IP Address, User Agent, Resource                         │ │
│  │ • Action, Result, Additional Details                       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## ⚡ Performance & Resilience Architecture

### **Performance Monitoring System**

```
┌─────────────────────────────────────────────────────────────────┐
│                  Performance Monitoring Layer                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  AOP-Based Performance Monitoring                               │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ @MonitorPerf.   │    │ Service Layer   │    │ Threshold   │ │
│  │ Annotation      │    │ Auto Monitoring │    │ Based       │ │
│  │                 │    │                 │    │ Logging     │ │
│  │ • Custom ops    │    │ • All service   │    │ • >100ms    │ │
│  │ • Configurable  │    │   methods       │    │   warnings  │ │
│  │ • Parameter     │    │ • Automatic     │    │ • Execution │ │
│  │   logging       │    │   tracking      │    │   time      │ │
│  │ • Threshold     │    │ • Error         │    │ • Success/  │ │
│  │   control       │    │   handling      │    │   failure   │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│                                                                 │
│           │                      │                      │       │
│           ▼                      ▼                      ▼       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Structured Performance Logging                │ │
│  │                                                             │ │
│  │ Log Levels & Patterns:                                     │ │
│  │ • INFO:  Normal operations                                 │ │
│  │ • WARN:  Slow operations (>100ms threshold)                │ │
│  │ • ERROR: Failed operations with timing                     │ │
│  │ • DEBUG: All operations (development only)                 │ │
│  │                                                             │ │
│  │ Log Format: OPERATION_TYPE: method - duration: Xms         │ │
│  │ Examples:                                                   │ │
│  │ • SERVICE_OPERATION: UserService.getUserById - 45ms        │ │
│  │ • SLOW_SERVICE_OPERATION: TransactionService.create - 150ms│ │
│  │ • PERFORMANCE_MONITOR: createTransaction - 75ms            │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Resilience Patterns Implementation**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Resilience Layer                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Rate Limiting (Bucket4j + Caffeine)                           │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ Token Bucket    │    │ Cache Storage   │    │ Policy      │ │
│  │ Algorithm       │    │ (Caffeine)      │    │ Management  │ │
│  │                 │    │                 │    │             │ │
│  │ • Smooth rate   │    │ • User-based    │    │ • Default:  │ │
│  │   limiting      │    │   buckets       │    │   100/min   │ │
│  │ • Burst         │    │ • In-memory     │    │ • Strict:   │ │
│  │   capacity      │    │   fast access   │    │   20/min    │ │
│  │ • Refill rate   │    │ • TTL: 1 hour   │    │ • Relaxed:  │ │
│  └─────────────────┘    └─────────────────┘    │   200/min   │ │
│                                                 └─────────────┘ │
│                                                                 │
│  Exception Handling & Translation                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Global Exception Handler (@ControllerAdvice)               │ │
│  │ • Infrastructure → Business exception translation          │ │
│  │ • Consistent error response format                         │ │
│  │ • HTTP status code mapping                                 │ │
│  │ • Detailed logging for debugging                           │ │
│  │ • Client-friendly error messages                           │ │
│  │                                                             │ │
│  │ Custom Exceptions:                                          │ │
│  │ • UserNotFoundException                                     │ │
│  │ • BankAccountNotFoundException                             │ │
│  │ • TransactionNotFoundException                             │ │
│  │ • CustomAccessDeniedException                              │ │
│  │ • IllegalArgumentException (validation failures)           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Thread Pool & Async Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                     Thread Management                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Web Layer (Embedded Tomcat)                                   │
│  ┌─────────────────┐                                           │
│  │ HTTP Threads    │ ◀── Configurable pool size                 │
│  │ (Tomcat)        │     • Default: 200 threads                │
│  │                 │     • Handle HTTP requests                │
│  │ • Request       │     • Short-lived operations              │
│  │   processing    │     • Immediate responses                 │
│  │ • Response      │     • Connection management               │
│  │   handling      │                                           │
│  └─────────────────┘                                           │
│           │                                                     │
│           ▼ (Delegate to Services)                              │
│                                                                 │
│  Service Layer (Transactional)                                 │
│  ┌─────────────────┐    ┌─────────────────┐                   │
│  │ User Operations │    │ Transaction     │                   │
│  │                 │    │ Operations      │                   │
│  │ • Synchronous   │    │                 │                   │
│  │ • Transactional │    │ • Synchronous   │                   │
│  │ • Fast response │    │ • ACID          │                   │
│  │ • User CRUD     │    │   compliance    │                   │
│  │ • Authentication│    │ • Balance       │                   │
│  └─────────────────┘    │   updates       │                   │
│                         └─────────────────┘                   │
│                                                                 │
│  Background Processing                                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ • Audit log processing (async where possible)              │ │
│  │ • Performance metrics collection                           │ │
│  │ • Database event logging                                   │ │
│  │ • Security event processing                                │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📊 Monitoring & Observability Architecture

### **Health Checks & Metrics Pipeline**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Application Health Monitoring               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Spring Boot Actuator Endpoints                                │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ /actuator/      │    │ /actuator/      │    │ /actuator/  │ │
│  │ health          │    │ metrics         │    │ info        │ │
│  │                 │    │                 │    │             │ │
│  │ • Application   │    │ • JVM Metrics   │    │ • App Info  │ │
│  │   Status        │    │ • HTTP Metrics  │    │ • Version   │ │
│  │ • Database      │    │ • Custom        │    │ • Build     │ │
│  │   Connectivity  │    │   Business      │    │   Details   │ │
│  │ • External      │    │   Metrics       │    │ • Contact   │ │
│  │   Dependencies  │    │ • Performance   │    │   Info      │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│                                                                 │
│           │                      │                      │       │
│           ▼                      ▼                      ▼       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Micrometer Metrics Collection                 │ │
│  │                                                             │ │
│  │ Business Metrics:                                          │ │
│  │ • Active user sessions                                     │ │
│  │ • Transaction counts by type                               │ │
│  │ • Account creation rates                                   │ │
│  │ • API endpoint hit counts                                  │ │
│  │ • Error rates by endpoint                                  │ │
│  │                                                             │ │
│  │ Technical Metrics:                                         │ │
│  │ • Response times (p50, p95, p99)                           │ │
│  │ • Throughput (requests per second)                         │ │
│  │ • JVM memory usage and GC metrics                          │ │
│  │ • Database connection pool statistics                      │ │
│  │ • Thread pool utilization                                  │ │
│  │                                                             │ │
│  │ Security Metrics:                                          │ │
│  │ • Authentication success/failure rates                     │ │
│  │ • Rate limiting violations                                 │ │
│  │ • Authorization failures by resource                       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Logging Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                       Logging Pipeline                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Application Logs (Logback + JSON)                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ Application     │    │ Performance     │    │ Security    │ │
│  │ Logs            │    │ Logs            │    │ Audit Logs  │ │
│  │                 │    │                 │    │             │ │
│  │ • Request/      │    │ • Method        │    │ • Auth      │ │
│  │   Response      │    │   execution     │    │   events    │ │
│  │ • Business      │    │   times         │    │ • Access    │ │
│  │   operations    │    │ • Slow          │    │   violations│ │
│  │ • Error logs    │    │   operations    │    │ • Rate      │ │
│  │ • Debug info    │    │ • Database      │    │   limiting  │ │
│  └─────────────────┘    │   performance   │    │ • User      │ │
│                         └─────────────────┘    │   actions   │ │
│                                                 └─────────────┘ │
│                                                                 │
│           │                      │                      │       │
│           ▼                      ▼                      ▼       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │             Structured JSON Logging Format                 │ │
│  │                                                             │ │
│  │ Common Fields:                                              │ │
│  │ • timestamp: ISO 8601 format                               │ │
│  │ • level: DEBUG, INFO, WARN, ERROR                          │ │
│  │ • logger: Class name                                       │ │
│  │ • thread: Thread name                                      │ │
│  │ • message: Human-readable message                          │ │
│  │                                                             │ │
│  │ Security Audit Fields:                                     │ │
│  │ • event: Security event type                               │ │
│  │ • userId: Authenticated user identifier                    │ │
│  │ • ipAddress: Client IP address                             │ │
│  │ • userAgent: Client user agent                             │ │
│  │ • resource: Accessed resource/endpoint                     │ │
│  │ • action: HTTP method or operation                         │ │
│  │ • result: SUCCESS, FAILURE, DENIED                         │ │
│  │                                                             │ │
│  │ Performance Fields:                                        │ │
│  │ • operation: Method or operation name                      │ │
│  │ • duration: Execution time in milliseconds                 │ │
│  │ • parameters: Method parameters (when enabled)             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Log Destinations                                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ File Appenders:                                             │ │
│  │ • eagle-bank-api-application.log (general application)     │ │
│  │ • eagle-bank-api-performance.log (performance metrics)     │ │
│  │ • eagle-bank-api-security.log (security events)           │ │
│  │ • eagle-bank-audit.log (audit trail)                      │ │
│  │                                                             │ │
│  │ Configuration:                                              │ │
│  │ • Rolling file policy (10MB max size)                      │ │
│  │ • 30-day retention                                         │ │
│  │ • Compression for archived files                           │ │
│  │ • Console output for development                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2. Kubernetes Architecture

The application includes Kubernetes deployment configurations using Kustomize:

```
k8s/
├── base/                  # Base configurations
│   ├── namespace.yaml     # Namespace definition
│   ├── deployment.yaml    # Application deployment
│   ├── service.yaml       # Service definition
│   ├── config.yaml        # ConfigMap
│   ├── postgres.yaml      # PostgreSQL deployment
│   └── redis.yaml         # Redis for session storage
├── staging/               # Staging environment
│   └── kustomization.yaml # Staging-specific configs
└── production/            # Production environment
    ├── kustomization.yaml # Production-specific configs
    ├── hpa-patch.yaml     # Horizontal Pod Autoscaler
    └── deployment-patch.yaml # Production deployment patches
```

## 🐳 Container & Deployment Architecture

### **Docker Container Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Compose Environment                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐ │
│  │   PostgreSQL    │  │    Keycloak     │  │  Eagle Bank API  │ │
│  │   Database      │  │  Auth Server    │  │                  │ │
│  │                 │  │                 │  │                  │ │
│  │ Port: 5432      │  │ Port: 8180      │  │ Port: 8080       │ │
│  │                 │  │                 │  │                  │ │
│  │ • Data Volume   │  │ • Admin UI      │  │ • REST API       │ │
│  │ • Health Checks │  │ • OAuth2/OIDC   │  │ • Swagger UI     │ │
│  │ • Custom Schema │  │ • User Mgmt     │  │ • Actuator       │ │
│  │ • Persistence   │  │ • Realm Config  │  │ • Health Checks  │ │
│  └─────────────────┘  └─────────────────┘  └──────────────────┘ │
│         │                       │                       │        │
│         ▼                       ▼                       ▼        │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              eagle-bank-network (Docker Bridge)            │ │
│  │                                                             │ │
│  │ • Internal DNS resolution                                  │ │
│  │ • Service discovery by container name                      │ │
│  │ • Isolated network for security                            │ │
│  │ • Health check dependencies                                │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Container Specifications:                                      │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ PostgreSQL:                                                 │ │
│  │ • Custom build with initialization scripts                  │ │
│  │ • eagle_bank database with eagle_user                      │ │
│  │ • Persistent volume for data                               │ │
│  │ • Health checks for readiness                              │ │
│  │                                                             │ │
│  │ Keycloak:                                                   │ │
│  │ • Official Keycloak 23.0 image                             │ │
│  │ • PostgreSQL backend for configuration storage             │ │
│  │ • Development mode with admin/admin credentials            │ │
│  │ • Ready for production with proper realm setup             │ │
│  │                                                             │ │
│  │ Eagle Bank API:                                             │ │
│  │ • OpenJDK 21 base image                                    │ │
│  │ • Spring Boot executable JAR                               │ │
│  │ • Externalized configuration                               │ │
│  │ • Non-root user execution                                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Kubernetes Deployment Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Namespace: eagle-bank                                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │  Ingress Layer                                              │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ • TLS termination                                       │ │ │
│  │  │ • Load balancing                                        │ │ │
│  │  │ • Rate limiting                                         │ │ │
│  │  │ • Path-based routing                                    │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                               │                               │ │
│  │                               ▼                               │ │
│  │  Service Layer                                              │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │  │Eagle Bank   │  │PostgreSQL   │  │ Redis (Optional)    │ │ │
│  │  │API Service  │  │Service      │  │ Service             │ │ │
│  │  │             │  │             │  │                     │ │ │
│  │  │• ClusterIP  │  │• ClusterIP  │  │ • ClusterIP         │ │ │
│  │  │• Port 8080  │  │• Port 5432  │  │ • Port 6379         │ │ │
│  │  │• Health     │  │• Headless   │  │ • Session storage   │ │ │
│  │  │  checks     │  │  for StatefulSet│ • Cache layer     │ │ │
│  │  └─────────────┘  └─────────────┘  └─────────────────────┘ │ │
│  │         │                 │                   │             │ │
│  │         ▼                 ▼                   ▼             │ │
│  │  Pod Layer                                                  │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │  │API Pods     │  │PostgreSQL   │  │ Redis Pods          │ │ │
│  │  │(Deployment) │  │(StatefulSet)│  │ (Deployment)        │ │ │
│  │  │             │  │             │  │                     │ │ │
│  │  │• 2-5 replicas│  │• 1 primary  │  │ • 2 replicas        │ │ │
│  │  │• Auto-scaling│  │• Persistent │  │ • HA configuration  │ │ │
│  │  │• Rolling    │  │  volumes    │  │ • Cluster mode      │ │ │
│  │  │  updates    │  │• Backup     │  │                     │ │ │
│  │  └─────────────┘  │  strategy   │  └─────────────────────┘ │ │
│  │                   └─────────────┘                          │ │
│  │                                                             │ │
│  │  Configuration Layer                                       │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ • ConfigMaps: Application configuration                │ │ │
│  │  │ • Secrets: Database credentials, JWT keys              │ │ │
│  │  │ • Persistent Volumes: Database storage                 │ │ │
│  │  │ • Network Policies: Security boundaries                │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Kustomize Structure:                                           │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ k8s/                                                        │ │
│  │ ├── base/                    # Base configurations         │ │
│  │ │   ├── namespace.yaml       # eagle-bank namespace        │ │
│  │ │   ├── deployment.yaml      # API deployment              │ │
│  │ │   ├── service.yaml         # ClusterIP service           │ │
│  │ │   ├── config.yaml          # ConfigMap                   │ │
│  │ │   ├── postgres.yaml        # Database StatefulSet       │ │
│  │ │   └── redis.yaml           # Cache deployment           │ │
│  │ │                                                          │ │
│  │ ├── staging/                 # Staging environment        │ │
│  │ │   ├── kustomization.yaml   # Staging overlays           │ │
│  │ │   └── deployment-patch.yaml# Resource limits            │ │
│  │ │                                                          │ │
│  │ └── production/              # Production environment     │ │
│  │     ├── kustomization.yaml   # Production overlays        │ │
│  │     ├── hpa-patch.yaml       # Horizontal Pod Autoscaler  │ │
│  │     ├── deployment-patch.yaml# Production optimizations   │ │
│  │     └── service-patch.yaml   # LoadBalancer service       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Auto-Scaling Strategy**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Auto-Scaling Configuration                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Horizontal Pod Autoscaler (HPA)                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Scaling Metrics:                                            │ │
│  │ • CPU Utilization (Target: 70%)                            │ │
│  │ • Memory Utilization (Target: 80%)                         │ │
│  │ • Custom Metrics (via Prometheus):                         │ │
│  │   - Request Rate (requests/second)                         │ │
│  │   - Response Time (p95 latency < 200ms)                    │ │
│  │   - Error Rate (< 1%)                                      │ │
│  │                                                             │ │
│  │ Scaling Behavior:                                           │ │
│  │ • Scale Up: Max 5 pods/2 minutes                          │ │
│  │ • Scale Down: Max 2 pods/5 minutes                        │ │
│  │ • Stabilization Window: 3 minutes                          │ │
│  │                                                             │ │
│  │ Environment Limits:                                         │ │
│  │ • Development: 1-2 pods                                    │ │
│  │ • Staging: 2-5 pods                                        │ │
│  │ • Production: 3-20 pods                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Resource Management                                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Resource Requests & Limits:                                 │ │
│  │                                                             │ │
│  │ Development:                                                │ │
│  │ • CPU Request: 100m, Limit: 500m                           │ │
│  │ • Memory Request: 256Mi, Limit: 512Mi                      │ │
│  │                                                             │ │
│  │ Staging:                                                    │ │
│  │ • CPU Request: 250m, Limit: 1000m                          │ │
│  │ • Memory Request: 512Mi, Limit: 1Gi                        │ │
│  │                                                             │ │
│  │ Production:                                                 │ │
│  │ • CPU Request: 500m, Limit: 2000m                          │ │
│  │ • Memory Request: 1Gi, Limit: 2Gi                          │ │
│  │                                                             │ │
│  │ Quality of Service: Burstable (requests < limits)          │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 API Design & Implementation

### **RESTful API Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                      API Endpoint Structure                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Base URL: /api/v1                                              │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ User Management:                                            │ │
│  │ ├── GET    /users              # List all users            │ │
│  │ ├── POST   /users              # Create new user           │ │
│  │ ├── GET    /users/{userId}     # Get user by ID            │ │
│  │ ├── PUT    /users/{userId}     # Update user               │ │
│  │ └── DELETE /users/{userId}     # Delete user               │ │
│  │                                                             │ │
│  │ Account Management:                                         │ │
│  │ ├── GET    /accounts           # List user's accounts      │ │
│  │ ├── POST   /accounts           # Create new account        │ │
│  │ ├── GET    /accounts/{accountNumber}  # Get account        │ │
│  │ ├── PATCH  /accounts/{accountNumber}  # Update account     │ │
│  │ └── DELETE /accounts/{accountNumber}  # Delete account     │ │
│  │                                                             │ │
│  │ Transaction Management:                                     │ │
│  │ ├── GET    /accounts/{accountNumber}/transactions          │ │
│  │ │          # List account transactions                     │ │
│  │ ├── POST   /accounts/{accountNumber}/transactions          │ │
│  │ │          # Create new transaction                        │ │
│  │ ├── GET    /accounts/{accountNumber}/transactions/{id}     │ │
│  │ │          # Get specific transaction                      │ │
│  │ ├── PUT    /accounts/{accountNumber}/transactions/{id}     │ │
│  │ │          # Update transaction                            │ │
│  │ └── DELETE /accounts/{accountNumber}/transactions/{id}     │ │
│  │            # Delete transaction                            │ │
│  │                                                             │ │
│  │ System Endpoints:                                           │ │
│  │ ├── GET    /actuator/health    # Health checks             │ │
│  │ ├── GET    /actuator/metrics   # Application metrics       │ │
│  │ ├── GET    /actuator/info      # Application information   │ │
│  │ └── GET    /swagger-ui/        # API documentation         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **API Security & Documentation**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Swagger/OpenAPI Integration                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Documentation Features:                                        │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ • Interactive API Testing Interface                         │ │
│  │ • Comprehensive endpoint documentation                      │ │
│  │ • Request/response schema definitions                       │ │
│  │ • Example requests and responses                            │ │
│  │ • Authentication integration (OAuth2 & Bearer JWT)         │ │
│  │ • Error response documentation                              │ │
│  │ • API versioning information                                │ │
│  │                                                             │ │
│  │ Security Schemes:                                           │ │
│  │ ├── bearerAuth (JWT Token)                                 │ │
│  │ │   • Header: Authorization: Bearer <token>                │ │
│  │ │   • JWT format with RS256 signature                      │ │
│  │ │   • Token obtained from Keycloak                         │ │
│  │ │                                                           │ │
│  │ └── oauth2 (OAuth2 Flow)                                   │ │
│  │     • Password grant flow                                  │ │
│  │     • Token URL: keycloak/realms/eagle-bank/token          │ │
│  │     • Scopes: openid, profile, email                      │ │
│  │                                                             │ │
│  │ Endpoint Protection:                                        │ │
│  │ • All endpoints require authentication except:             │ │
│  │   - GET /users (public user listing)                       │ │
│  │   - POST /users (user registration)                        │ │
│  │   - Actuator health endpoints                              │ │
│  │   - Swagger UI and API documentation                       │ │
│  │                                                             │ │
│  │ Authorization Strategy:                                     │ │
│  │ • Resource ownership validation                            │ │
│  │ • Users can only access their own data                     │ │
│  │ • Email-based user identification from JWT                 │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Error Handling & Response Format**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Global Exception Handling                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  HTTP Status Code Mapping:                                      │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Success Responses:                                          │ │
│  │ • 200 OK - Successful GET, PUT operations                  │ │
│  │ • 201 Created - Successful POST operations                 │ │
│  │ • 204 No Content - Successful DELETE operations            │ │
│  │                                                             │ │
│  │ Client Error Responses:                                     │ │
│  │ • 400 Bad Request - Validation failures, invalid input     │ │
│  │ • 401 Unauthorized - Missing or invalid JWT token          │ │
│  │ • 403 Forbidden - Access denied, insufficient permissions  │ │
│  │ • 404 Not Found - Resource not found                       │ │
│  │ • 409 Conflict - Business rule violations                  │ │
│  │ • 429 Too Many Requests - Rate limit exceeded              │ │
│  │                                                             │ │
│  │ Server Error Responses:                                     │ │
│  │ • 500 Internal Server Error - Unexpected server errors     │ │
│  │ • 503 Service Unavailable - Database connectivity issues   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Error Response Format:                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Standard Error Response:                                    │ │
│  │ {                                                           │ │
│  │   "message": "User not found with ID: usr-123",            │ │
│  │   "timestamp": "2025-08-18T10:30:00",                      │ │
│  │   "path": "/api/v1/users/usr-123",                         │ │
│  │   "method": "GET"                                           │ │
│  │ }                                                           │ │
│  │                                                             │ │
│  │ Validation Error Response:                                  │ │
│  │ {                                                           │ │
│  │   "message": "Validation failed for request",              │ │
│  │   "timestamp": "2025-08-18T10:30:00",                      │ │
│  │   "path": "/api/v1/users",                                 │ │
│  │   "method": "POST",                                         │ │
│  │   "validationErrors": [                                     │ │
│  │     {                                                       │ │
│  │       "field": "email",                                     │ │
│  │       "message": "Email must be valid",                    │ │
│  │       "rejectedValue": "invalid-email"                     │ │
│  │     }                                                       │ │
│  │   ]                                                         │ │
│  │ }                                                           │ │
│  │                                                             │ │
│  │ Security Error Response:                                    │ │
│  │ {                                                           │ │
│  │   "message": "Access Denied",                              │ │
│  │   "timestamp": "2025-08-18T10:30:00"                       │ │
│  │ }                                                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🛡️ Security Implementation Details

### **Multi-Layered Security Approach**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Security Implementation                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Input Validation & Sanitization                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Bean Validation (Jakarta Validation):                      │ │
│  │ • @NotNull, @NotBlank for required fields                  │ │
│  │ • @Email for email format validation                       │ │
│  │ • @Pattern for phone number validation                     │ │
│  │ • @Size for string length constraints                      │ │
│  │ • @DecimalMin/@DecimalMax for numeric ranges               │ │
│  │ • Custom validators for business rules                     │ │
│  │                                                             │ │
│  │ Request/Response Validation:                                │ │
│  │ • DTO validation on all endpoints                          │ │
│  │ • Path variable validation                                 │ │
│  │ • Query parameter validation                               │ │
│  │ • Nested object validation                                 │ │
│  │                                                             │ │
│  │ SQL Injection Prevention:                                   │ │
│  │ • JPA parameterized queries only                           │ │
│  │ • No dynamic SQL construction                              │ │
│  │ • Repository-based data access                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Authentication & Authorization                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ JWT Token Processing:                                       │ │
│  │ • RS256 signature validation                               │ │
│  │ • Token expiration checking                                │ │
│  │ • Issuer validation (Keycloak)                             │ │
│  │ • Audience validation                                      │ │
│  │ • Claims extraction (sub, email, roles)                    │ │
│  │                                                             │ │
│  │ Authorization Strategy:                                     │ │
│  │ • Resource ownership validation                            │ │
│  │ • Email-based user identification                          │ │
│  │ • Automatic permission checking                            │ │
│  │ • Cross-user access prevention                             │ │
│  │                                                             │ │
│  │ Security Filters:                                           │ │
│  │ • Rate limiting filter (first in chain)                    │ │
│  │ • JWT authentication filter                                │ │
│  │ • Authorization filter                                     │ │
│  │ • Security audit interceptor                               │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Data Protection & Privacy                                      │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Sensitive Data Handling:                                    │ │
│  │ • Email masking in logs (LoggingUtils.maskEmail)           │ │
│  │ • User ID masking in logs                                  │ │
│  │ • No password storage (external auth via Keycloak)         │ │
│  │ • Secure ID generation (UUID-based)                        │ │
│  │                                                             │ │
│  │ Database Security:                                          │ │
│  │ • Separate database user with limited privileges           │ │
│  │ • Connection encryption in production                      │ │
│  │ • Audit logging for all database operations                │ │
│  │ • No direct database access for application users          │ │
│  │                                                             │ │
│  │ Configuration Security:                                     │ │
│  │ • Externalized configuration                               │ │
│  │ • Environment-based secrets                                │ │
│  │ • No hardcoded credentials                                 │ │
│  │ • Secure defaults for all settings                         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📈 Performance Optimization & Scalability

### **Caching Strategy**

```
┌─────────────────────────────────────────────────────────────────┐
│                        Caching Architecture                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Application Layer Caching                                      │
│  ┌─────────────────┐                                           │
│  │ Rate Limit      │ ◀── Caffeine (In-Memory Cache)             │
│  │ Cache           │     • Sub-millisecond access               │
│  │                 │     • 10,000 user buckets max              │
│  │ • Token Buckets │     • TTL: 1 hour                          │
│  │ • Per-user      │     • LRU eviction policy                  │
│  │   tracking      │     • Thread-safe operations               │
│  │ • Fast lookup   │                                            │
│  └─────────────────┘                                           │
│           │                                                     │
│           ▼ (Future Enhancement)                                │
│  ┌─────────────────┐                                           │
│  │ JPA Second      │ ◀── Hibernate L2 Cache (Optional)          │
│  │ Level Cache     │     • Entity caching                      │
│  │                 │     • Query result caching                │
│  │ • User entities │     • Region-based cache management       │
│  │ • Account info  │     • Configurable TTL                    │
│  │ • Static data   │                                            │
│  └─────────────────┘                                           │
│                                                                 │
│  Database Query Optimization                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ JPA Query Optimization:                                     │ │
│  │ • Named queries for complex operations                      │ │
│  │ • Fetch join strategies for related entities               │ │
│  │ • Pagination for large result sets                         │ │
│  │ • Index hints for performance-critical queries             │ │
│  │                                                             │ │
│  │ Database Indexes:                                           │ │
│  │ • Primary key indexes (automatic)                          │ │
│  │ • Unique indexes on email, account number                  │ │
│  │ • Foreign key indexes for relationships                    │ │
│  │ • Composite indexes for common query patterns              │ │
│  │                                                             │ │
│  │ Connection Pool Management:                                 │ │
│  │ • Optimized pool size based on load                        │ │
│  │ • Connection validation queries                            │ │
│  │ • Idle connection timeout                                  │ │
│  │ • Maximum connection lifetime                              │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Scalability Design Patterns**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Horizontal Scalability                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Stateless Application Design                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Session Management:                                         │ │
│  │ • No server-side session storage                           │ │
│  │ • JWT-based stateless authentication                       │ │
│  │ • All state in database or external cache                  │ │
│  │ • Load balancer friendly                                   │ │
│  │                                                             │ │
│  │ Resource Management:                                        │ │
│  │ • Configurable thread pools                                │ │
│  │ • Connection pooling with limits                           │ │
│  │ • Memory management tuning                                 │ │
│  │ • GC optimization for low latency                          │ │
│  │                                                             │ │
│  │ Configuration Externalization:                              │ │
│  │ • Environment-based configuration                          │ │
│  │ • Runtime property updates                                 │ │
│  │ • Profile-based settings                                   │ │
│  │ • Kubernetes ConfigMap integration                         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Auto-Scaling Integration                                       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Kubernetes HPA Metrics:                                     │ │
│  │ • CPU utilization (target: 70%)                            │ │
│  │ • Memory utilization (target: 80%)                         │ │
│  │ • Custom business metrics                                  │ │
│  │ • Request latency thresholds                               │ │
│  │                                                             │ │
│  │ Graceful Shutdown:                                          │ │
│  │ • Spring Boot shutdown hooks                               │ │
│  │ • Connection draining                                      │ │
│  │ • In-flight request completion                             │ │
│  │ • Resource cleanup                                         │ │
│  │                                                             │ │
│  │ Health Check Integration:                                   │ │
│  │ • Kubernetes readiness probes                              │ │
│  │ • Kubernetes liveness probes                               │ │
│  │ • Custom health indicators                                 │ │
│  │ • Dependency health validation                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Development & Testing Architecture

### **Development Workflow**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Development Environment                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Dev Container Setup                                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ .devcontainer/ Configuration:                               │ │
│  │ • Ubuntu 22.04 base image                                  │ │
│  │ • OpenJDK 21 with Maven                                    │ │
│  │ • Docker-in-Docker support                                 │ │
│  │ • VS Code extensions pre-installed                         │ │
│  │ • Git configuration                                        │ │
│  │                                                             │ │
│  │ Development Tools:                                          │ │
│  │ • Spring Boot DevTools (auto-restart)                      │ │
│  │ • H2 Console for database inspection                       │ │
│  │ • Live reload for static resources                         │ │
│  │ • Debug configuration                                      │ │
│  │                                                             │ │
│  │ Local Services:                                             │ │
│  │ • Docker Compose for dependencies                          │ │
│  │ • PostgreSQL database                                      │ │
│  │ • Keycloak authentication server                           │ │
│  │ • Redis (optional for caching)                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Code Quality & Standards                                       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Code Style & Formatting:                                    │ │
│  │ • Google Java Style Guide                                  │ │
│  │ • Automatic formatting with Spotless                       │ │
│  │ • Import organization                                      │ │
│  │ • Line length limits                                       │ │
│  │                                                             │ │
│  │ Static Analysis:                                            │ │
│  │ • SpotBugs for bug detection                               │ │
│  │ • PMD for code quality                                     │ │
│  │ • Checkstyle for coding standards                          │ │
│  │ • SonarQube integration (optional)                         │ │
│  │                                                             │ │
│  │ Documentation:                                              │ │
│  │ • Comprehensive README.md                                  │ │
│  │ • API documentation with Swagger                           │ │
│  │ • Inline code documentation                                │ │
│  │ • Architecture diagrams                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Testing Strategy & Implementation**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Testing Pyramid                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    E2E Tests                                │ │
│  │                  (Few, Slow)                                │ │
│  │                                                             │ │
│  │ • Full application testing                                 │ │
│  │ • Real database and services                               │ │
│  │ • User journey validation                                  │ │
│  │ • Critical path testing                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                 │
│                               ▼                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Integration Tests                            │ │
│  │                 (Some, Medium)                              │ │
│  │                                                             │ │
│  │ Test Categories:                                            │ │
│  │ • @SpringBootTest - Full context loading                   │ │
│  │ • @WebMvcTest - Controller layer testing                   │ │
│  │ • @DataJpaTest - Repository layer testing                  │ │
│  │ • @TestContainers - Real database testing                  │ │
│  │                                                             │ │
│  │ Test Configuration:                                         │ │
│  │ • H2 in-memory database                                    │ │
│  │ • Mock external dependencies                               │ │
│  │ • Test-specific profiles                                   │ │
│  │ • Isolated test data                                       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                 │
│                               ▼                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    Unit Tests                               │ │
│  │                  (Many, Fast)                               │ │
│  │                                                             │ │
│  │ Testing Framework:                                          │ │
│  │ • JUnit 5 (Jupiter) - Test framework                       │ │
│  │ • Mockito - Mocking framework                              │ │
│  │ • AssertJ - Fluent assertions                              │ │
│  │ • WireMock - HTTP service mocking                          │ │
│  │                                                             │ │
│  │ Coverage Areas:                                             │ │
│  │ • Service layer business logic                             │ │
│  │ • Repository custom queries                                │ │
│  │ • Utility classes and helpers                              │ │
│  │ • Exception handling logic                                 │ │
│  │ • Validation logic                                         │ │
│  │                                                             │ │
│  │ Test Organization:                                          │ │
│  │ • Mirror source package structure                          │ │
│  │ • Test classes end with "Test"                             │ │
│  │ • Nested test classes for grouping                         │ │
│  │ • DisplayName annotations for clarity                      │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Test Data Management                                           │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Test Data Strategy:                                         │ │
│  │ • Builder pattern for test objects                         │ │
│  │ • Factory methods for common scenarios                     │ │
│  │ • @Sql scripts for database setup                          │ │
│  │ • @Transactional for test isolation                        │ │
│  │                                                             │ │
│  │ Security Testing:                                           │ │
│  │ • @WithMockUser for authentication                         │ │
│  │ • JWT token simulation                                     │ │
│  │ • Authorization testing                                    │ │
│  │ • Rate limiting validation                                 │ │
│  │                                                             │ │
│  │ Performance Testing:                                        │ │
│  │ • @Test(timeout) for performance bounds                    │ │
│  │ • Memory usage validation                                  │ │
│  │ • Load testing with JMeter (separate)                     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Future Architecture Enhancements

### **Microservices Evolution Strategy**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Microservices Decomposition                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Phase 1: Current Monolith (Microservices Ready)               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Single Deployable Unit:                                     │ │
│  │ • Layered architecture                                      │ │
│  │ • Clear service boundaries                                 │ │
│  │ • Interface-based design                                   │ │
│  │ • Stateless application logic                              │ │
│  │ • External configuration                                   │ │
│  │                                                             │ │
│  │ Preparation for Decomposition:                              │ │
│  │ • Domain-driven design principles                          │ │
│  │ • Bounded context identification                           │ │
│  │ • Database per service readiness                           │ │
│  │ • Event-driven communication patterns                      │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Phase 2: Service Decomposition (Future)                       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Proposed Services:                                          │ │
│  │                                                             │ │
│  │ ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │ │ User        │  │ Account     │  │ Transaction         │ │ │
│  │ │ Service     │  │ Service     │  │ Service             │ │ │
│  │ │             │  │             │  │                     │ │ │
│  │ │• User CRUD  │  │• Account    │  │• Transaction        │ │ │
│  │ │• Profile    │  │  management │  │  processing         │ │ │
│  │ │  management │  │• Balance    │  │• Transfer logic     │ │ │
│  │ │• Auth       │  │  tracking   │  │• Audit trail        │ │ │
│  │ │  integration│  │• Account    │  │• Notification       │ │ │
│  │ └─────────────┘  │  types      │  │  triggers           │ │ │
│  │                  └─────────────┘  └─────────────────────┘ │ │
│  │                                                             │ │
│  │ ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │ │ Auth        │  │ Notification│  │ Audit & Monitoring  │ │ │
│  │ │ Service     │  │ Service     │  │ Service             │ │ │
│  │ │             │  │             │  │                     │ │ │
│  │ │• JWT        │  │• Email/SMS  │  │• Security events    │ │ │
│  │ │  validation │  │• Push       │  │• Performance        │ │ │
│  │ │• Rate       │  │  notifications│ │  metrics            │ │ │
│  │ │  limiting   │  │• Templates  │  │• Compliance         │ │ │
│  │ │• Security   │  │• Delivery   │  │  reporting          │ │ │
│  │ │  audit      │  │  tracking   │  │• Analytics          │ │ │
│  │ └─────────────┘  └─────────────┘  └─────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Phase 3: Cloud-Native Patterns (Advanced)                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                                                             │ │
│  │ Event-Driven Architecture:                                  │ │
│  │ • Apache Kafka for event streaming                         │ │
│  │ • Event sourcing for audit trails                          │ │
│  │ • CQRS for read/write separation                           │ │
│  │ • Saga pattern for distributed transactions                │ │
│  │                                                             │ │
│  │ Advanced Resilience:                                        │ │
│  │ • Service mesh (Istio) for traffic management              │ │
│  │ • Distributed tracing (Jaeger/Zipkin)                      │ │
│  │ • Chaos engineering for fault tolerance                    │ │
│  │ • Advanced circuit breaker patterns                        │ │
│  │                                                             │ │
│  │ Data Management:                                            │ │
│  │ • Database per service                                     │ │
│  │ • Event-driven data synchronization                        │ │
│  │ • Polyglot persistence                                     │ │
│  │ • Data lake for analytics                                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📋 Conclusion

The Eagle Bank API architecture represents a modern, enterprise-grade approach to building secure, scalable, and maintainable banking applications. The architecture emphasizes:

### **Key Architectural Strengths**

- **🔒 Security First**: Multi-layered security with OAuth2/JWT, comprehensive auditing, and rate limiting
- **📈 Performance Optimized**: AOP-based monitoring, caching strategies, and auto-scaling capabilities  
- **🏗️ Maintainable Design**: Clear layered architecture with domain-driven design principles
- **🔧 Developer Experience**: Comprehensive testing, dev container support, and extensive documentation
- **☁️ Cloud Ready**: Kubernetes-native deployment with container-first design
- **📊 Observable**: Structured logging, health checks, and metrics collection
- **🛡️ Resilient**: Exception handling, circuit breaker patterns, and graceful degradation

### **Technology Excellence**

The application leverages modern Java 21 features, Spring Boot 3.2.0 ecosystem, and cloud-native patterns to deliver:

- **Sub-100ms response times** for most operations
- **99.9% availability** through health checks and auto-scaling
- **Enterprise security** with Keycloak integration and comprehensive audit trails
- **Horizontal scalability** with stateless design and Kubernetes deployment
- **Developer productivity** with comprehensive testing and development tools

### **Future-Ready Architecture**

The design supports evolution towards:

- **Microservices decomposition** with clear service boundaries
- **Event-driven architecture** for real-time processing
- **Advanced cloud-native patterns** including service mesh and chaos engineering
- **Machine learning integration** for fraud detection and analytics

This architecture provides a solid foundation for building modern banking applications that can scale with business growth while maintaining security, performance, and reliability standards expected in the financial services industry.
