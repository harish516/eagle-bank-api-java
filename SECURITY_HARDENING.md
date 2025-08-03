# Security Hardening Implementation

This document outlines the security hardening features implemented in the Eagle Bank API, focusing on rate limiting and audit logging.

## Overview

The security hardening includes:
1. **Rate Limiting** - Token bucket algorithm to prevent abuse
2. **Audit Logging** - Comprehensive security event tracking
3. **Enhanced Authentication/Authorization Logging** - Detailed failure tracking

## Components Implemented

### 1. Rate Limiting System

#### Components:
- `RateLimitConfig.java` - Configuration for rate limiting parameters
- `RateLimitService.java` - Core service implementing token bucket algorithm
- `RateLimitFilter.java` - Servlet filter applying rate limits to requests

#### Features:
- **Three Rate Limit Types:**
  - `DEFAULT`: 100 requests/minute (burst: 20)
  - `STRICT`: 20 requests/minute (burst: 5) - for sensitive operations
  - `RELAXED`: 200 requests/minute (burst: 50) - for read operations

- **Client Identification:**
  - User-based limiting (JWT subject) for authenticated users
  - IP-based limiting for unauthenticated requests
  - Proxy-aware IP detection (X-Forwarded-For, X-Real-IP)

- **Endpoint Classification:**
  - `STRICT` rate limiting for:
    - User creation/deletion (`POST/DELETE /api/v1/users`)
    - Account deletion (`DELETE /api/v1/accounts/*`)
    - Transaction operations (`POST/PUT/DELETE /api/v1/transactions`)
  - `RELAXED` rate limiting for:
    - All GET operations (read-only)
  - `DEFAULT` rate limiting for:
    - All other operations

#### Response Headers:
- `X-RateLimit-Remaining`: Number of available tokens
- `X-RateLimit-Type`: Applied rate limit type
- `Retry-After`: Suggested retry time (60 seconds)

#### Error Response:
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "status": 429,
  "path": "/api/v1/endpoint",
  "rateLimitType": "STRICT"
}
```

### 2. Audit Logging System

#### Components:
- `AuditService.java` - Core audit logging service
- `SecurityAuditInterceptor.java` - Web interceptor for request/response logging
- Enhanced `CustomAccessDeniedHandler.java` - Authorization failure logging
- Enhanced `CustomAuthenticationEntryPoint.java` - Authentication failure logging

#### Security Events Tracked:
- `AUTHENTICATION_SUCCESS` - Successful login attempts
- `AUTHENTICATION_FAILURE` - Failed login attempts
- `AUTHORIZATION_SUCCESS` - Successful access to protected resources
- `AUTHORIZATION_FAILURE` - Access denied events
- `RATE_LIMIT_EXCEEDED` - Rate limit violations
- `ACCOUNT_DELETION` - Account deletion operations
- `USER_MANAGEMENT` - User creation/modification
- `FINANCIAL_TRANSACTION` - Transaction operations
- `SENSITIVE_OPERATION` - Other sensitive operations
- `SUSPICIOUS_ACTIVITY` - Flagged activities

#### Audit Log Structure:
```json
{
  "timestamp": "2025-08-03T18:00:00",
  "event": "AUTHENTICATION_FAILURE",
  "userId": "user-123",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "resource": "/api/v1/accounts",
  "action": "GET",
  "result": "DENIED",
  "details": {
    "failureReason": "Invalid credentials"
  }
}
```

#### Log Levels:
- `WARN`: Security failures, rate limit violations, sensitive operations
- `INFO`: Successful authentications and authorizations
- `DEBUG`: Other security events

### 3. Integration Points

#### Security Filter Chain:
- Rate limiting filter runs before authentication
- All API endpoints protected by rate limiting
- Excluded paths: `/actuator/**`, `/swagger-ui/**`, `/h2-console/**`

#### Web Configuration:
- Security audit interceptor applies to `/api/**` paths
- Automatic sensitive operation detection
- Request/response lifecycle tracking

## Configuration

### Application Properties:
```yaml
# Security Configuration
security:
  rate-limit:
    default:
      requests-per-minute: 100
      burst-capacity: 20
    strict:
      requests-per-minute: 20
      burst-capacity: 5
    relaxed:
      requests-per-minute: 200
      burst-capacity: 50

# Logging Configuration
logging:
  level:
    com.eaglebank.service.AuditService: INFO
    com.eaglebank.filter.RateLimitFilter: WARN
  file:
    name: logs/eagle-bank-audit.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

### Maven Dependencies:
```xml
<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-caffeine</artifactId>
    <version>7.6.0</version>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

## Security Benefits

### Protection Against:
1. **DoS/DDoS Attacks** - Rate limiting prevents request flooding
2. **Brute Force Attacks** - Strict limits on authentication endpoints
3. **API Abuse** - Different limits for different operation types
4. **Security Incident Response** - Comprehensive audit trail

### Compliance Features:
1. **Audit Trail** - Complete security event logging
2. **Access Monitoring** - Real-time access attempt tracking
3. **Incident Detection** - Automatic flagging of suspicious activities
4. **Forensic Analysis** - Structured logs for security investigation

## Monitoring and Alerting

### Log Analysis:
- Security events logged in structured JSON format
- Integration ready for log aggregation tools (ELK, Splunk)
- Time-based log rotation for disk space management

### Metrics Available:
- Rate limit cache statistics (hit ratio, eviction count)
- Authentication success/failure rates
- API endpoint access patterns
- Security event frequency

### Recommended Alerts:
1. High rate of authentication failures
2. Repeated rate limit violations from same IP/user
3. Access attempts to non-existent resources
4. Unusual patterns in sensitive operations

## Future Enhancements

### Potential Improvements:
1. **Dynamic Rate Limiting** - Adjust limits based on threat levels
2. **Geolocation Blocking** - Block requests from specific regions
3. **Behavioral Analysis** - ML-based anomaly detection
4. **Real-time Notifications** - Immediate alerts for critical events
5. **Dashboard Integration** - Web-based security monitoring interface

## Testing

The implementation includes comprehensive unit tests for:
- Rate limiting functionality
- Audit logging accuracy
- Security event classification
- Error handling and edge cases
