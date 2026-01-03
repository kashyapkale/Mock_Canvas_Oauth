# Mock Canvas OAuth Service

🎯 **A lightweight Mock Canvas LMS OAuth 2.0 service for development and testing purposes.**

This service provides a complete OAuth 2.0 authorization flow implementation that mimics Canvas LMS authentication, allowing developers to test Canvas integrations without requiring access to a real Canvas instance.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [OAuth Flow](#oauth-flow)
- [API Endpoints](#api-endpoints)
- [Deployment](#deployment)
- [Testing](#testing)
- [Security Notes](#security-notes)
- [Contributing](#contributing)

## 🎯 Overview

The Mock Canvas OAuth Service is designed for developers who need to:
- **Test Canvas LMS integrations** without a real Canvas instance
- **Develop locally** without Canvas API credentials
- **Automate testing** with predictable OAuth responses
- **Prototype** Canvas-based applications quickly

This service implements the standard OAuth 2.0 authorization code flow used by Canvas LMS, making it a drop-in replacement for Canvas OAuth during development.

## ✨ Features

- ✅ **OAuth 2.0 Authorization Code Flow** - Full implementation matching Canvas LMS
- ✅ **JWT Token Generation** - Access tokens and refresh tokens
- ✅ **Multiple User Roles** - Pre-configured instructor and student accounts
- ✅ **Token Validation** - Endpoint to validate access tokens
- ✅ **CORS Support** - Ready for frontend integration
- ✅ **H2 Database** - Lightweight in-memory database for token storage
- ✅ **Environment-Based Configuration** - Secure token management via environment variables
- ✅ **Health Check Endpoint** - Monitor service status
- ✅ **Spring Boot 3** - Modern Java framework with auto-configuration

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (required)
- **Maven 3.6+** (included via Maven Wrapper)
- **Port 8457** available (configurable)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd fake_oauth_canvas
   ```

2. **Set environment variables**
   
   Create a `.env` file or set environment variables:
   ```bash
   # Required OAuth Tokens
   export OAUTH_INSTRUCTOR_ACCESS_TOKEN="4511~your-instructor-token-here"
   export OAUTH_STUDENT_ACCESS_TOKEN="4511~your-student-token-here"
   
   # Optional (has defaults)
   export SERVER_PORT=8457
   export OAUTH_CLIENT_ID="fake-client-id"
   export OAUTH_CLIENT_SECRET="fake-client-secret"
   ```

   Or use `application.properties` for local development (see [Configuration](#configuration)).

3. **Build and run**
   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw clean package
   java -jar target/fake_oauth_canvas-0.0.1-SNAPSHOT.jar
   
   # Or run directly with Maven
   ./mvnw spring-boot:run
   ```

4. **Verify it's running**
   ```bash
   curl http://localhost:8457/health
   # Should return: {"status":"ok",...}
   ```

The service will be available at:
- 🚀 **Main Service**: `http://localhost:8457`
- 📱 **Authorization URL**: `http://localhost:8457/login/oauth2/auth`
- 🔑 **Token URL**: `http://localhost:8457/login/oauth2/token`
- 🏥 **Health Check**: `http://localhost:8457/health`

## ⚙️ Configuration

### Environment Variables

All configuration can be done via environment variables or `application.properties`. Environment variables take precedence.

#### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `OAUTH_INSTRUCTOR_ACCESS_TOKEN` | Instructor access token | `4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV` |
| `OAUTH_STUDENT_ACCESS_TOKEN` | Student access token | `4511~BZDWGxJvCmMvTyQfUEXnGLJavQttXBEk8EPCvUKCN3aJL3CX79DYVF3xxaLUeKu6` |

#### Optional Variables (with defaults)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8457` | Server port |
| `PROVIDER_BASE_URL` | `http://localhost:8457` | Base URL for OAuth provider |
| `OAUTH_CLIENT_ID` | `fake-client-id` | OAuth client identifier |
| `OAUTH_CLIENT_SECRET` | `fake-client-secret` | OAuth client secret |
| `OAUTH_INSTRUCTOR_ID` | `101` | Instructor user ID |
| `OAUTH_INSTRUCTOR_NAME` | `Kashyap Kale` | Instructor display name |
| `OAUTH_INSTRUCTOR_REFRESH_TOKEN` | `refresh-token-instructor-67890` | Instructor refresh token |
| `OAUTH_STUDENT_ID` | `202` | Student user ID |
| `OAUTH_STUDENT_NAME` | `Sarthak Raut` | Student display name |
| `OAUTH_STUDENT_REFRESH_TOKEN` | `refresh-token-student-fghij` | Student refresh token |

### Application Properties

For local development, you can also configure via `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=${PORT:${SERVER_PORT:8457}}

# OAuth Tokens (REQUIRED - no defaults for security)
oauth.instructor.access-token=${OAUTH_INSTRUCTOR_ACCESS_TOKEN}
oauth.student.access-token=${OAUTH_STUDENT_ACCESS_TOKEN}

# OAuth Client
spring.security.oauth2.client.registration.generic.client-id=${OAUTH_CLIENT_ID:fake-client-id}
spring.security.oauth2.client.registration.generic.client-secret=${OAUTH_CLIENT_SECRET:fake-client-secret}
```

## 🔄 OAuth Flow

### Step 1: Authorization Request

Redirect users to the authorization endpoint:

```javascript
const authUrl = 'http://localhost:8457/login/oauth2/auth?' + 
  new URLSearchParams({
    client_id: 'fake-client-id',
    response_type: 'code',
    redirect_uri: 'http://localhost:3000/auth/callback',
    state: 'random_state_value',
    scope: 'user:read'
  });

window.location.href = authUrl;
```

### Step 2: User Selection

Users will see a login selection page where they can choose:
- **Instructor** role
- **Student** role

### Step 3: Authorization Code Exchange

After selection, users are redirected to your callback URL with an authorization code:

```
http://localhost:3000/auth/callback?code=AUTHORIZATION_CODE&state=random_state_value
```

### Step 4: Token Exchange

Exchange the authorization code for access tokens:

```javascript
const response = await fetch('http://localhost:8457/login/oauth2/token', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  body: new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: 'fake-client-id',
    client_secret: 'fake-client-secret',
    code: 'AUTHORIZATION_CODE',
    redirect_uri: 'http://localhost:3000/auth/callback'
  })
});

const tokens = await response.json();
// Response:
// {
//   "access_token": "4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV",
//   "refresh_token": "refresh-token-instructor-67890",
//   "token_type": "Bearer",
//   "user": {
//     "id": 101,
//     "name": "Kashyap Kale"
//   }
// }
```

### Step 5: Token Refresh

Refresh access tokens using refresh tokens:

```javascript
const response = await fetch('http://localhost:8457/login/oauth2/token', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  body: new URLSearchParams({
    grant_type: 'refresh_token',
    client_id: 'fake-client-id',
    client_secret: 'fake-client-secret',
    refresh_token: 'REFRESH_TOKEN'
  })
});
```

## 📡 API Endpoints

### GET `/login/oauth2/auth`

**Purpose**: Show OAuth authorization page

**Query Parameters**:
- `client_id` (required): Client identifier
- `response_type` (required): Must be "code"
- `redirect_uri` (required): Redirect URI after authorization
- `state` (optional): State parameter for CSRF protection
- `scope` (optional): Requested scope

**Response**: HTML authorization page

---

### POST `/login/oauth2/auth`

**Purpose**: Handle user role selection

**Body Parameters**:
- `role` (required): "instructor" or "student"
- OAuth parameters from GET request

**Response**: Redirect to `redirect_uri` with authorization code

---

### POST `/login/oauth2/token`

**Purpose**: Exchange code for tokens or refresh tokens

**Authorization Code Grant**:
```json
{
  "grant_type": "authorization_code",
  "client_id": "fake-client-id",
  "client_secret": "fake-client-secret",
  "code": "AUTHORIZATION_CODE",
  "redirect_uri": "http://localhost:3000/auth/callback"
}
```

**Refresh Token Grant**:
```json
{
  "grant_type": "refresh_token",
  "client_id": "fake-client-id",
  "client_secret": "fake-client-secret",
  "refresh_token": "REFRESH_TOKEN"
}
```

**Response**:
```json
{
  "access_token": "4511~...",
  "refresh_token": "refresh-token-...",
  "token_type": "Bearer",
  "user": {
    "id": 101,
    "name": "Kashyap Kale"
  }
}
```

---

### GET `/api/v1/users/self`

**Purpose**: Validate access token and return user information

**Headers**:
```
Authorization: Bearer 4511~your-access-token-here
```

**Query Parameters**:
- `access_token` (optional): Alternative to Bearer token in header

**Response**:
```json
{
  "id": 101,
  "name": "Kashyap Kale",
  "login_id": "instructor",
  "canvas_user_id": 101
}
```

---

### GET `/health`

**Purpose**: Health check endpoint

**Response**:
```json
{
  "status": "ok",
  "service": "Mock Canvas OAuth Service",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

---

### GET `/`

**Purpose**: Service information

**Response**:
```json
{
  "service": "Mock Canvas OAuth Service",
  "version": "1.0.0",
  "endpoints": {
    "authorization": "/login/oauth2/auth",
    "token": "/login/oauth2/token",
    "health": "/health"
  }
}
```

## 🚢 Deployment

This service can be deployed to various platforms. See [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed instructions.

### Quick Deploy Options

1. **Railway** (Recommended) - [railway.app](https://railway.app)
2. **Render** - [render.com](https://render.com)
3. **Fly.io** - [fly.io](https://fly.io)

### Environment Variables for Production

⚠️ **Important**: Always set OAuth tokens via environment variables in production. Never commit tokens to version control.

```bash
# Required
OAUTH_INSTRUCTOR_ACCESS_TOKEN=your-instructor-token
OAUTH_STUDENT_ACCESS_TOKEN=your-student-token

# Server URL (update after deployment)
PROVIDER_BASE_URL=https://your-deployed-url.com

# OAuth Client
OAUTH_CLIENT_ID=your-client-id
OAUTH_CLIENT_SECRET=your-client-secret
```

## 🧪 Testing

### Manual Testing

1. **Test Authorization Flow**:
   ```bash
   # Open in browser
   http://localhost:8457/login/oauth2/auth?client_id=fake-client-id&response_type=code&redirect_uri=http://localhost:3000/callback
   ```

2. **Test Token Exchange**:
   ```bash
   curl -X POST http://localhost:8457/login/oauth2/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=authorization_code&client_id=fake-client-id&client_secret=fake-client-secret&code=YOUR_CODE&redirect_uri=http://localhost:3000/callback"
   ```

3. **Test Token Validation**:
   ```bash
   curl http://localhost:8457/api/v1/users/self?access_token=4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV
   ```

### Integration Testing

Use this service in your test suite:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CanvasIntegrationTest {
    
    @Value("${local.server.port}")
    private int port;
    
    @Test
    void testOAuthFlow() {
        String baseUrl = "http://localhost:" + port;
        // Test OAuth flow...
    }
}
```

## 🔒 Security Notes

⚠️ **This is a development/testing service only. Do NOT use in production.**

- All credentials are configurable but visible in environment variables
- Tokens are not cryptographically secure (they're mock tokens)
- No rate limiting or security hardening
- H2 database is in-memory (data resets on restart)
- CORS is permissive by default

**For Production**: Use the real Canvas LMS OAuth endpoints at `https://canvas.instructure.com`

## 🤝 Contributing

Contributions are welcome! This is a mock service for development purposes.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

MIT License - feel free to use this in your projects!

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Inspired by Canvas LMS OAuth 2.0 implementation
- Designed for the VT AI Lecture Assistant project

## 📚 Additional Resources

- [Canvas LMS OAuth Documentation](https://canvas.instructure.com/doc/api/file.oauth.html)
- [OAuth 2.0 Specification](https://oauth.net/2/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)

---

**Need Help?** Open an issue on GitHub or check the [DEPLOYMENT.md](./DEPLOYMENT.md) guide for deployment instructions.

