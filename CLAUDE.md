# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building and Running
```bash
# Build the project
./gradlew build

# Run the application (local profile)
./gradlew bootRun

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Generate QueryDSL Q-classes
./gradlew compileJava
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.gamegoo.gamegoo_v2.service.matching.MatchingServiceTest"

# Run tests with specific profile
./gradlew test -Dspring.profiles.active=test
```

## Architecture Overview

This is a Spring Boot application for a League of Legends player matching platform. The architecture follows Domain-Driven Design (DDD) principles with a clear separation of concerns.

### Core Domains

1. **Account** - User management, authentication (JWT), email verification
2. **Matching** - Complex matchmaking algorithm based on game statistics and preferences
3. **Chat** - Real-time messaging between matched players
4. **Social** - Friend system, blocking, manner rating system
5. **Game** - League of Legends champions and game styles
6. **Content** - User-generated boards and reporting system

### Key Architectural Patterns

#### Facade Service Pattern
Complex business operations are orchestrated through FacadeServices that coordinate multiple domain services:
- `MatchingFacadeService` - Handles matching workflow and chatroom creation
- `AuthFacadeService` - Coordinates authentication and member management
- `ChatFacadeService` - Manages chat operations and validations

#### Domain Service Layer
Each domain has dedicated services:
- **CommandService** - Write operations
- **QueryService** - Read operations  
- **FacadeService** - Cross-domain orchestration

#### Validation Architecture
Centralized validators handle cross-domain business rules:
- `MemberValidator` - User existence and status checks
- `BlockValidator` - User blocking verification
- `MatchingValidator` - Matching state validation

### Security Architecture

- **JWT-based authentication** with refresh tokens
- **JwtAuthFilter** for request authentication
- **Role-based authorization** (member/admin roles)
- **SecurityUtil** for accessing current user context

### Database and Persistence

- **JPA + QueryDSL** for complex queries
- **Custom repository implementations** in `*RepositoryCustomImpl` classes
- **BaseDateTimeEntity** for automatic timestamp tracking
- **MySQL** for production, **H2** for testing

### External Integrations

- **Riot Games API** - OAuth2 integration and real-time game data
- **Socket Server** - Real-time communication support
- **Email Service** - Gmail SMTP integration
- **Discord Logging** - Error monitoring and notifications

### Event-Driven Components

Spring Events are used for decoupled communication:
- Friend request notifications
- Manner rating calculations
- Email sending operations
- Socket join events

## Development Guidelines

### Working with the Codebase

1. **Start with Controllers** - API endpoints define the application boundaries
2. **Use FacadeServices** - For operations spanning multiple domains
3. **Leverage Validators** - Ensure business rules are consistently applied
4. **Custom Repositories** - Use QueryDSL for complex database queries

### Environment Profiles

- **local** - Local development with MySQL
- **dev** - Development environment
- **prod** - Production environment  
- **test** - Testing with H2 in-memory database

### Required Environment Variables

For local development, ensure these environment variables are set:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `JWT_SECRET` - JWT token signing
- `RIOT_API` - Riot Games API key
- `CLIENT_ID`, `CLIENT_SECRET` - Riot OAuth credentials
- `GMAIL_PWD` - Email service password

### Testing Approach

- **Integration tests** for FacadeServices (test cross-domain workflows)
- **Unit tests** for business logic components
- **Repository tests** for data access layer with `@DataJpaTest`
- **Controller tests** with security context using custom annotations

### Code Conventions

- Use **@RequiredArgsConstructor** for dependency injection
- **@Transactional(readOnly = true)** on query services
- **ApiResponse** wrapper for all REST responses
- **ErrorCode** enumeration for consistent error handling
- **Q-classes** are auto-generated in `src/main/generated/`

### Key Files to Understand

- `SecurityConfig.java` - Security configuration and JWT setup
- `*FacadeService.java` - Complex business workflows
- `*Validator.java` - Cross-domain business rules
- `application.yml` - Multi-profile configuration
- `ExceptionAdvice.java` - Global error handling