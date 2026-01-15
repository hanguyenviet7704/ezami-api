# Ezami Server

A Spring Boot REST API server for the Ezami platform - an educational quiz and career development application.

## Overview

Ezami Server is a backend application built with Spring Boot that provides RESTful APIs for managing users, quizzes, posts, and educational content. The platform integrates with WordPress for content management and includes features like JWT authentication, Google OAuth, quiz management, user activities tracking, and payment integration with RevenueCat.

## Tech Stack

- **Java 17**
- **Spring Boot 2.7.5** - Core framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Spring Data Redis** - Caching and session management
- **MySQL 5.7+** - Primary database (WordPress database integration)
- **Redis 7** - Cache and session storage
- **JWT (jjwt 0.9.1)** - Token-based authentication
- **Google OAuth 2.0** - Social authentication
- **Gmail API** - Email services for notifications and verification codes
- **RevenueCat** - Payment and subscription management
- **Swagger/OpenAPI 3** - API documentation
- **Docker & Docker Compose** - Containerization
- **Gradle 7.6+** - Build automation

## Features

- 🔐 **JWT Authentication** - Secure user authentication with JWT tokens
- 🔑 **Google OAuth Integration** - Social login via Google (support for web and mobile platforms)
- 👤 **User Management** - User registration, profile management, and activity tracking
- ✉️ **Email Verification** - Email-based verification code system for registration and password reset
- 📝 **Quiz System** - Comprehensive quiz management with:
  - Quiz categories and types (full test, mini test, parts)
  - Question management
  - Quiz submission and scoring
  - Statistics and performance tracking
- 📰 **Content Management** - Integration with WordPress for posts and articles:
  - Article spaces and categories
  - Post search and filtering
  - Category-based content organization
- 💰 **Payment Integration** - RevenueCat integration for:
  - Subscription management
  - Webhook handling for payment events
  - Purchase tracking
- 📧 **Email Services** - Gmail API and SMTP integration for:
  - Verification code delivery
  - Notification emails
- 📊 **Analytics** - User activity and quiz performance tracking
- 🔄 **API Versioning** - Support for API versioning
- 📚 **Swagger Documentation** - Interactive API documentation at `/swagger-ui.html`
- ⚡ **Caching** - Redis-based caching for improved performance

## Database Schema

The application integrates with WordPress database and includes custom entities:

### User Management
- `User` - User accounts and authentication
- `UserActivity` - User activity tracking
- `UserActivityMeta` - Additional activity metadata
- `UserPurchased` - Purchase and subscription records
- `VerificationCode` - Email verification codes

### Quiz System
- `QuizMaster` - Main quiz information
- `QuestionEntity` - Quiz questions
- `QuizCategoryEntity` - Quiz categories
- `QuizStatisticEntity` - User quiz statistics
- `QuizStatisticRefEntity` - Quiz statistics references

### Content Management
- `Post` - WordPress posts/articles
- `PostMeta` - Post metadata
- `ArticleSpaceEntity` - Article spaces/collections
- `ArticleSpaceCategoryEntity` - Article space categories

### Taxonomy System
- `TermEntity` - Terms/tags
- `TermTaxonomyEntity` - Taxonomy definitions
- `TermRelationshipEntity` - Term relationships
- `TermRelationshipId` - Composite key for relationships

### System
- `VersionEntity` - API version information
- `AppEntity` - Application metadata

## Prerequisites

- **Java 17+**
- **Docker** and **Docker Compose**
- **MySQL 5.7+**
- **Gradle 7.6+**
- **Git**

## Installation & Setup

### Option 1: Quick Start with Docker Compose (Recommended)

#### 1. Clone the repository
```bash
git clone <repository-url>
cd ezami-api
```

#### 2. Configure environment variables (Optional)
Create a `.env` file in the root directory:
```bash
# JWT Configuration
JWT_SECRET=your-secret-key-change-this-in-production

# Spring Profile (dev, release)
SPRING_PROFILES_ACTIVE=dev

# Mail Configuration (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@ezami.io
MAIL_FROM_NAME=Ezami
```

#### 3. Start all services (MySQL, Redis, and Application)
```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# View application logs only
docker-compose logs -f app
```

#### 4. Access the application
- **API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

#### 5. Stop all services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Option 2: Local Development Setup

#### 1. Start only MySQL and Redis
```bash
docker-compose up -d mysql redis
```

#### 2. Configure application properties
Edit the configuration files in `src/main/resources/`:
- `application.yaml` - Local development
- `application-dev.yaml` - Development environment
- `application-release.yaml` - Production environment

Key configurations to update:
- Database connection settings
- JWT secret key
- Gmail API credentials
- RevenueCat API keys

#### 3. Run the application locally

For development:
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate - WARNING: deletes all data)
docker-compose down -v
```

### Option 2: Local Development Setup

#### 1. Start only MySQL and Redis
```bash
docker-compose up -d mysql redis
```

#### 2. Configure application properties

Edit the configuration files in `src/main/resources/`:
- `application.yaml` - Base configuration (uses environment variables)
- `application-release.yaml` - Production environment configuration

Key configurations to update via environment variables:
- Database connection settings (`DB_DRIVER`, `DB_URL`, `DB_USER`, `DB_PASS`)
- JWT secret key (`JWT_SECRET`)
- Google OAuth credentials (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, etc.)
- RevenueCat API keys (`REVENUECAT_SECRET_API_KEY`, etc.)
- Mail configuration (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`)
- Redis configuration (`SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`, `SPRING_REDIS_PASSWORD`)

#### 3. Run the application locally

For development:
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

For production build:
```bash
# Windows
gradlew.bat bootRun --args='--spring.profiles.active=release'

# Linux/Mac
./run.sh
```

The application will start on `http://localhost:8080`

## API Endpoints

All API endpoints are prefixed with `/api/` (except authentication endpoints).

### Authentication (`/authenticate`, `/signup`, `/register`, `/auth/google/*`)

- `POST /authenticate` - User login (returns JWT token)
  - Request: `{ "username": "email@example.com", "password": "password" }`
  - Response: `{ "token": "jwt-token-here" }`

- `POST /signup` - Legacy user registration (without verification code)
- `POST /register` - User registration with verification code
  - Request: `{ "email": "email@example.com", "password": "password", "verificationCode": "123456", "appCode": "ezami" }`

- `POST /verification-code` - Generate and send verification code
  - Request: `{ "email": "email@example.com", "type": "REGISTER" }`
  - Types: `REGISTER`, `RESET_PASSWORD`

- `POST /verification-code/check` - Validate verification code
  - Request: `{ "email": "email@example.com", "verificationCode": "123456", "type": "REGISTER" }`

- `GET /auth/google/login` - Get Google OAuth login URL
  - Query params: `platform` (mobile/web)
  - Response: `{ "authorizationUrl": "https://accounts.google.com/..." }`

- `GET /auth/google/callback` - Google OAuth callback (handled automatically by Google)

### User Management (`/api/user/*`)

Requires JWT authentication (Bearer token in Authorization header).

- `GET /api/user/info` - Get current user profile
- `PUT /api/user/profile` - Update user profile
- `GET /api/user/activities` - Get user activities

### Quiz Management (`/api/quiz/*`)

Requires JWT authentication.

- `GET /api/quiz` - Search quizzes
  - Query params: `category` (optional), `typeTest` (optional)
  - Examples: `/api/quiz?category=toeic-reading&typeTest=full`

- `GET /api/quiz/category` - Get all quiz categories

- `GET /api/quiz/{id}` - Get quiz details (including all questions)

- `POST /api/quiz/{id}/submit` - Submit quiz answers
  - Request: `{ "answers": [{"questionId": 1, "answer": "A"}, ...] }`
  - Response: Score, percentage, pass/fail status, correct answers

- `GET /api/quiz/statistics` - Get quiz statistics for current user

### Content Management (`/api/post/*`)

Public endpoints (no authentication required).

- `GET /api/post` - Search posts/articles
  - Query params: `categoryId` (optional)
  - Example: `/api/post?categoryId=5`

- `GET /api/post/{id}` - Get post/article details

- `GET /api/article-space` - Get all article spaces
  - Query params: `language` (optional, default: "vn"), `appCode` (optional, default: "ezami"), `withCategory` (optional, default: true)

### Version Management (`/api/version`)

- `GET /api/version` - Get API version information

### Webhooks (`/webhook/*`)

- `POST /webhook/revenuecat` - RevenueCat webhook handler (requires signature verification)
- `GET /webhook/revenuecat/ping` - Webhook health check

## Configuration

### Database Configuration

The application uses WordPress database schema. Configure via environment variables:

```yaml
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_URL=jdbc:mysql://localhost:3307/wordpress
DB_USER=wordpress
DB_PASS=your_password
```

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:your_jwt_secret_key}
```

### Redis Configuration

```yaml
spring:
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:12345678aA@}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### Google OAuth Configuration

```yaml
google:
  oauth2:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}
    mobile-success-url: ${GOOGLE_REDIRECT_MOBILE_SUCCESS_URL}
    web-success-url: ${GOOGLE_REDIRECT_WEB_SUCCESS_URL}
```

### RevenueCat Configuration

```yaml
revenueCat:
  apiBaseUrl: ${REVENUECAT_API_BASE_URL}
  secretApiKey: ${REVENUECAT_SECRET_API_KEY}
  webhookSecret: ${REVENUECAT_WEBHOOK_SECRET}
  endpoints:
    subscribers: ${REVENUECAT_SUBSCRIBERS_ENDPOINT}
```

### Mail Configuration

```yaml
mail:
  smtp:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    from: ${MAIL_FROM:noreply@ezami.io}
    from.name: ${MAIL_FROM_NAME:Ezami}
```

## Development

### Running Tests

```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

### Building the Application

```bash
# Build JAR file
./gradlew build

# Build without tests
./gradlew build -x test

# Build JAR (includes tests)
./gradlew bootJar
```

The JAR file will be generated at: `build/libs/ezami-server-0.0.1-SNAPSHOT.jar`

### Code Structure

```
src/main/java/com/hth/udecareer/
├── annotation/          # Custom annotations (ApiPrefixController, ApiV2PrefixController)
├── config/              # Application configuration
│   ├── AppConfig.java
│   ├── GoogleOAuthConfig.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WordpressDatasourceConfiguration.java
├── controllers/         # REST API controllers
│   ├── JwtAuthenticationController.java
│   ├── PostController.java
│   ├── QuizController.java
│   ├── UserController.java
│   ├── VersionController.java
│   └── WebhookController.java
├── entities/            # JPA entities (database models)
├── repository/          # Data access layer (Spring Data JPA repositories)
├── service/             # Business logic layer
│   ├── Impl/           # Service implementations
│   ├── EmailCoreService.java
│   ├── EmailSMTPService.java
│   ├── GoogleAuthServiceImpl.java
│   ├── PostService.java
│   ├── QuizCategoryService.java
│   ├── QuizMasterService.java
│   ├── QuizResultFormService.java
│   ├── RevenueCatService.java
│   ├── RevenueCatWebhookServiceImpl.java
│   ├── UserService.java
│   ├── VerificationCodeService.java
│   └── VersionService.java
├── security/            # Security configuration
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtRequest.java
│   ├── JwtRequestFilter.java
│   ├── JwtResponse.java
│   ├── JwtTokenUtil.java
│   ├── JwtUserDetailsService.java
│   └── PhpPasswordEncoder.java
├── model/               # DTOs and request/response models
│   ├── dto/            # Data Transfer Objects
│   ├── request/       # Request models
│   └── response/      # Response models
├── exception/           # Custom exceptions (AppException)
├── enums/               # Enumerations (ErrorCode, PostStatus, QuizType, etc.)
├── converter/           # Entity-DTO converters
├── utils/               # Utility classes
└── UdecareerServerApplication.java  # Main application class
```

## Docker Deployment

The application includes Docker Compose configuration for easy deployment with three services:
- **MySQL 5.7**: Primary database (port 3307)
- **Redis 7**: Cache and session storage (port 6379)
- **Spring Boot App**: Application server (port 8080)

### Docker Compose Services

```yaml
services:
  mysql:    # MySQL database with health checks
  redis:    # Redis cache with persistence
  app:      # Spring Boot application
```

### Useful Docker Commands

```bash
# Build and start all services
docker-compose up -d --build

# Start specific service
docker-compose up -d app

# Restart application service
docker-compose restart app

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app

# Execute commands inside container
docker-compose exec app sh

# Check service status
docker-compose ps

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# Rebuild application image
docker-compose build app
```

### Health Checks

All services include health checks:
- **MySQL**: Checks database connectivity
- **Redis**: Checks Redis connectivity
- **App**: Checks Spring Boot Actuator health endpoint

The application will wait for MySQL and Redis to be healthy before starting (`depends_on` with `condition: service_healthy`).

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Write unit tests for new features
- Update API documentation (Swagger annotations)
- Follow the existing code structure and patterns

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the API documentation at `/swagger-ui.html`

## Changelog

### Version 0.0.1-SNAPSHOT
- Initial release
- JWT authentication
- Google OAuth integration
- Quiz management system
- Content management with WordPress integration
- RevenueCat payment integration
- Email verification system
- User activity tracking
- Redis caching
- Docker deployment support

