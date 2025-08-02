# Eagle Bank API - Swagger Documentation

## Overview

The Eagle Bank API now includes **Swagger UI** for interactive API documentation and testing. You can view and test all API endpoints directly from your browser without needing external tools like Postman.

## Accessing Swagger UI

### 1. Start the Application

```bash
./mvnw spring-boot:run
```

### 2. Open Swagger UI in Browser

Once the application is running, navigate to:

**Swagger UI**: http://localhost:8080/eagle-bank/swagger-ui.html

**OpenAPI JSON**: http://localhost:8080/eagle-bank/api-docs

## Quick Setup for Testing

### 1. Start Required Services

```bash
# Start the application
./mvnw spring-boot:run

# Ensure Keycloak is running (in another terminal)
sudo docker-compose up -d
```

### 2. Create a Test User in Keycloak (One-time setup)

1. **Access Keycloak Admin Console**: http://localhost:8180/admin/
2. **Login** with admin credentials (admin/admin)
3. **Select the "eagle-bank" realm**
4. **Create a user**:
   - Go to **Users** → **Add User**
   - Username: `testuser`
   - Email: `test@example.com`
   - Save
5. **Set password**:
   - Go to **Credentials** tab
   - Set password: `Test1234`
   - Turn off "Temporary" 
   - Save

### 3. Test the API

1. **Open Swagger UI**: http://localhost:8080/eagle-bank/swagger-ui.html
2. **Click "Authorize"** and use OAuth2 with username: `testuser`, password: `Test1234`
3. **Test any protected endpoint!**

## Using Swagger UI

### Features Available

✅ **Interactive API Testing** - Execute API calls directly from the browser  
✅ **Comprehensive Documentation** - View detailed endpoint descriptions  
✅ **Request/Response Examples** - See sample data for all endpoints  
✅ **OAuth2 Authentication** - Login with username/password (no manual token required!)  
✅ **JWT Bearer Token Support** - Manual token entry for advanced users  
✅ **Schema Validation** - View data models and validation rules  

### API Endpoints Available

#### 🔓 **Public Endpoints** (No Authentication Required)
- `POST /api/v1/users` - Create a new user account
- `GET /api/v1/users` - Get all users (Admin operation)

#### 🔒 **Protected Endpoints** (JWT Authentication Required)
- `GET /api/v1/users/{userId}` - Get user by ID (own data only)
- `PATCH /api/v1/users/{userId}` - Update user (own data only)
- `DELETE /api/v1/users/{userId}` - Delete user (own account only)
- `GET /api/v1/users/me` - Get current authenticated user

## Testing with Authentication

### Option 1: OAuth2 Login (Recommended - Username & Password)

The easiest way to test protected endpoints is using OAuth2 authentication directly in Swagger UI:

1. Click the **"Authorize"** button (🔒 icon) at the top of Swagger UI
2. You'll see two authentication options:
   - **oauth2** - For username/password login
   - **bearerAuth** - For manual JWT token entry

3. **For OAuth2 (Username/Password)**:
   - Click the **"Authorize"** button under "oauth2"
   - Enter your Keycloak credentials:
     - **Username**: `testuser` (or your Keycloak username)
     - **Password**: `Test1234` (or your Keycloak password)
     - **Client ID**: `eagle-bank-api` (pre-filled)
   - Click **"Authorize"**
   - The system will automatically get the JWT token for you

4. **Test any protected endpoint** - you're now authenticated!

### Option 2: Manual JWT Token Entry

If you prefer to handle JWT tokens manually:

### Step 1: Create a User (No Auth Required)

1. Expand `POST /api/v1/users`
2. Click **"Try it out"**
3. Use this sample request body:

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "address": {
    "line1": "123 Main Street",
    "line2": "Apartment 4B",
    "town": "London",
    "county": "Greater London",
    "postcode": "SW1A 1AA"
  }
}
```

4. Click **"Execute"**
5. Note the returned `userId` from the response

### Step 2: Get JWT Token (From Keycloak)

For protected endpoints, you need a JWT token. You can obtain one by:

1. **Using Keycloak Admin Console** (for testing):
   - Access: http://localhost:8180/admin/
   - Login with admin credentials
   - Create a test user or use existing one
   - Generate a token for that user

2. **Using Keycloak REST API**:
   ```bash
   curl -X POST http://localhost:8180/realms/eagle-bank/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password" \
     -d "client_id=eagle-bank-api" \
     -d "username=your-username" \
     -d "password=your-password"
   ```

### Step 3: Authenticate in Swagger UI

1. Click the **"Authorize"** button (🔒 icon) at the top of Swagger UI
2. In the "bearerAuth" section, enter: `Bearer YOUR_JWT_TOKEN`
3. Click **"Authorize"**
4. Click **"Close"**

### Step 4: Test Protected Endpoints

Now you can test any protected endpoint:

1. Try `GET /api/v1/users/me` to get your user details
2. Try `GET /api/v1/users/{userId}` with your own user ID
3. Try `PATCH /api/v1/users/{userId}` to update your details

## Data Models

### User Creation Request
```json
{
  "name": "string (required)",
  "email": "string (required, valid email)",
  "phoneNumber": "string (required, international format)",
  "address": {
    "line1": "string (required)",
    "line2": "string (optional)",
    "line3": "string (optional)",
    "town": "string (required)",
    "county": "string (required)",
    "postcode": "string (required, UK format)"
  }
}
```

### User Response
```json
{
  "id": "string",
  "name": "string",
  "email": "string",
  "phoneNumber": "string",
  "address": { ... },
  "createdTimestamp": "2024-01-15T10:30:00",
  "updatedTimestamp": "2024-01-15T10:30:00"
}
```

## Security Features

### JWT Authentication
- **Token Format**: Bearer JWT
- **Issuer**: Keycloak (http://localhost:8180/realms/eagle-bank)
- **Validation**: Email-based user identification
- **Authorization**: Users can only access/modify their own data

### Endpoint Security
- **Public**: User registration (`POST /users`)
- **Protected**: All other user operations
- **Ownership Validation**: Automated check that users access only their own data

## Error Responses

### Common HTTP Status Codes
- **200 OK** - Successful operation
- **201 Created** - User created successfully
- **204 No Content** - User deleted successfully
- **400 Bad Request** - Invalid input data
- **401 Unauthorized** - Missing or invalid JWT token
- **403 Forbidden** - Access denied (e.g., accessing another user's data)
- **404 Not Found** - User not found
- **409 Conflict** - Email already exists or cannot delete user with accounts

## Advanced Usage

### Custom Server URLs
The API documentation includes server configurations for:
- **Local Development**: http://localhost:8080
- **Production**: https://api.eaglebank.com

### API Versioning
All endpoints use version prefix `/api/v1/` for future compatibility.

### OpenAPI Specification
The full OpenAPI 3.0.1 specification is available at:
http://localhost:8080/eagle-bank/api-docs

## Troubleshooting

### Common Issues

1. **"Swagger UI not loading"**
   - Ensure the application is running on port 8080
   - Check that the URL includes the context path: `/eagle-bank/`

2. **"401 Unauthorized on protected endpoints"**
   - Ensure you've clicked "Authorize" and entered a valid JWT token
   - Verify the token format: `Bearer YOUR_JWT_TOKEN`

3. **"403 Forbidden when accessing user data"**
   - Users can only access their own data
   - Ensure the JWT token contains the correct email claim

4. **"Connection refused to Keycloak"**
   - Ensure Keycloak is running on port 8180
   - Check docker-compose setup for Keycloak service

### Development Notes

The Swagger UI is available in all environments and provides:
- **Real-time API testing** without external tools
- **Comprehensive documentation** with examples
- **Schema validation** for request/response formats
- **Authentication testing** with JWT tokens

This makes the Eagle Bank API much more accessible for developers and testers! 🚀
