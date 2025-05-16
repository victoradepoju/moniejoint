# Moniejoint - Microservices Architecture

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
   - [Microservices Breakdown](#microservices-breakdown)
   - [Communication Flow](#communication-flow)
3. [Features](#features)
4. [API Documentation](#api-documentation)
   - [Authentication Service](#authentication-service)
   - [User Service](#user-service)
   - [Wallet Service](#wallet-service)
   - [Saving Group Service](#saving-group-service)
5. [Scheduled Jobs](#scheduled-jobs)
6. [Configuration](#configuration)
7. [Deployment](#deployment)
8. [Error Handling](#error-handling)
9. [Security](#security)
10. [Testing](#testing)
11. [Future Enhancements](#future-enhancements)

## Project Overview

The Joint Savings Platform is a microservices-based application that enables users to create and participate in various types of savings groups with customizable payout structures. The system supports:

- **Rotating Savings (Ajo/Esusu)**: Members contribute regularly and receive payouts in a defined order
- **Fixed Deposit**: Members contribute to a common pool that earns interest
- **Target Savings**: Members save towards a specific financial goal

Key functionalities include:
- User registration and authentication
- Digital wallet management
- Group creation and management
- Invitation system
- Automated contribution processing
- Flexible payout structures

## Architecture

### Microservices Breakdown

1. **api-gateway**: Spring Cloud Gateway for routing requests to appropriate services
2. **auth-service**: Handles user authentication and JWT generation
3. **config-server**: Centralized configuration management
4. **eureka-server**: Service discovery and registration
5. **filestorage-service**: Manages file uploads (profile pictures) to GCP Storage
6. **user-service**: Manages user profiles and details
7. **wallet-service**: Handles wallet operations and transactions
8. **saving-group-service**: Core service for savings group management

### Communication Flow

```
Client → API Gateway → [Auth Service | User Service | Wallet Service | Saving Group Service]
                                      ↑               ↑
                                      │               │
                                      ↓               ↓
                                Config Server ← Eureka Server
```

Services communicate via:
- Synchronous REST calls (for immediate operations)
- Asynchronous events (for eventual consistency)
- Service discovery via Eureka

## Features

### Savings Group Types

1. **ROTATING**:
   - Members contribute fixed amounts at regular intervals
   - Payouts follow a defined order (random or creator-defined)
   - Example: Traditional rotating savings schemes

2. **FIXED_DEPOSIT**:
   - Members contribute to a common pool
   - Funds are held for a fixed term
   - Earnings are distributed proportionally

3. **TARGET_SAVING**:
   - Members save towards a specific target amount
   - Funds are disbursed when target is reached

### Contribution Frequencies

- TWO_MINUTES (for testing)
- DAILY
- WEEKLY
- BI_WEEKLY
- MONTHLY

### Payout Order Types

- RANDOM: System randomly assigns payout order
- CREATOR_DEFINED: Group creator specifies payout sequence

## API Documentation

### Authentication Service

#### Register User
`POST /v1/auth/register`

Request:
```json
{
  "username": "garvann1",
  "password": "password1",
  "email": "garvann1@gmail.com"
}
```

Response:
```json
{
  "id": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
  "username": "garvann1",
  "email": "garvann1@gmail.com",
  "userDetails": {},
  "walletInfo": {
    "walletId": "1d965ce5-c53b-425a-8076-479c566a6162",
    "userId": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
    "balance": 0,
    "currency": "NGN",
    "status": "ACTIVE",
    "createdAt": "2025-04-14T09:27:55.857007"
  }
}
```

#### Login
`POST /v1/auth/login`

Request:
```json
{
  "username": "victoradepoju",
  "password": "newpassword1"
}
```

Response:
```json
{
  "token": "jwt_token"
}
```

### User Service

#### Update User Details
`PUT /v1/user/update` (Multipart)

Request:
```json
{
  "id": "54c97947-a24b-4de3-8a9a-7dc99caa2f9d",
  "userDetails": {
    "country": "United State"
  }
}
```
+ File attachment for profile picture

Response:
```json
{
  "id": "54c97947-a24b-4de3-8a9a-7dc99caa2f9d",
  "username": "victoradepoju",
  "email": "victoradepoju30@gmail.com",
  "userDetails": {
    "firstName": "Victor",
    "lastName": "Adepoju",
    "phoneNumber": "1234567890",
    "country": "United State",
    "address": "123 Street",
    "aboutMe": "I am a Backend Engineer",
    "profilePicture": "https://storage.cloud.google.com/ms-filestorage-test/e0bb59a2-e813-4d67-9ba3-e7d0a70ce822.png"
  }
}
```

#### Get User by Username
`GET /v1/user/getUserByUsername/{username}`

Response:
```json
{
  "id": "20464fea-b208-4d48-8b4d-83885c579457",
  "username": "victoradepoju",
  "password": "$2a$10$89X7k6Lj6ubzcV0Bo7cl9eycVVQMeUJs/AEmulxLdxnsoNJTRdUaG",
  "role": "USER"
}
```

### Wallet Service

#### Get Wallet by User ID
`GET /v1/wallets/user/{userId}`

Response:
```json
{
  "walletId": "wallet-1",
  "userId": "user-1",
  "balance": 16500,
  "currency": "NGN",
  "status": "ACTIVE",
  "createdAt": null
}
```

#### Deposit Funds
`POST /v1/wallets/{walletId}/deposit`

Request:
```json
{
  "amount": 15000,
  "initiatedBy": "SYSTEM"
}
```

Response:
```json
{
  "transactionId": "7c360f21-c592-4bd3-8290-6bc244cae5e5",
  "walletId": "1d965ce5-c53b-425a-8076-479c566a6162",
  "amount": 15000,
  "currency": "NGN",
  "type": "DEPOSIT",
  "status": "COMPLETED",
  "reference": null,
  "description": null,
  "initiatedBy": "SYSTEM",
  "createdAt": null
}
```

#### Transfer Funds
`POST /v1/wallets/transfer`

Request:
```json
{
  "sourceWalletId": "wallet-1",
  "destinationWalletId": "f5195380-e233-4929-8ae9-78cf1d3153d3",
  "amount": 200,
  "initiatedBy": "user-1"
}
```

Response:
```json
{
  "debitTransaction": {
    "transactionId": "e4561d3c-7cef-4225-99a7-5bd69205d7a9",
    "walletId": "wallet-1",
    "amount": 200,
    "currency": "NGN",
    "type": "TRANSFER",
    "status": "COMPLETED",
    "reference": null,
    "description": "Transfer to f5195380-e233-4929-8ae9-78cf1d3153d3",
    "initiatedBy": "user-1",
    "createdAt": null
  },
  "creditTransaction": {
    "transactionId": "ff9d99c7-a77f-4849-8906-4af9781e84b0",
    "walletId": "f5195380-e233-4929-8ae9-78cf1d3153d3",
    "amount": 200,
    "currency": "NGN",
    "type": "TRANSFER",
    "status": "COMPLETED",
    "reference": null,
    "description": "Transfer from wallet-1",
    "initiatedBy": "user-1",
    "createdAt": null
  }
}
```

### Saving Group Service

#### Create Group
`POST /v1/groups`

Request:
```json
{
  "name": "Ajo",
  "description": "Monthly family savings group",
  "creatorId": "user-1",
  "type": "ROTATING",
  "minimumContribution": 500,
  "minimumJoinAmount": 1000,
  "maxParticipants": 3,
  "contributionAmount": 500,
  "frequency": "TWO_MINUTES",
  "payoutOrderType": "RANDOM"
}
```

Response:
```json
{
  "groupId": "ad751a6f-70c6-4cc5-acb3-491d33094aff",
  "name": "Ajo",
  "description": "Monthly family savings group",
  "creatorId": "user-1",
  "type": "ROTATING",
  "minimumContribution": 500,
  "minimumJoinAmount": 1000,
  "maxParticipants": 3,
  "contributionAmount": 500,
  "frequency": "TWO_MINUTES",
  "payoutOrderType": "RANDOM",
  "status": "FORMING",
  "nextContributionDate": "2025-04-14T09:22:59.110045",
  "currentRound": 1,
  "targetAmount": null,
  "memberCount": 1,
  "createdAt": null
}
```

#### Create Group Invite
`POST /v1/groups/{groupId}/invites`

Request:
```json
{
  "invitedUserId": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
  "invitedByUserId": "user-1",
  "minimumAmountRequired": 15000
}
```

Response:
```json
{
  "inviteId": 3,
  "groupId": "ad751a6f-70c6-4cc5-acb3-491d33094aff",
  "inviteCode": "762342",
  "invitedUserId": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
  "invitedByUserId": "user-1",
  "minimumAmountRequired": 15000,
  "status": "PENDING",
  "expiryDate": "2025-04-21T09:29:50.129925",
  "createdAt": null
}
```

#### Join Group with Invite
`POST /v1/groups/join/{inviteCode}`

Request:
```json
{
  "userId": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
  "initialAmount": 5000
}
```

Response:
```json
{
  "memberId": 3,
  "groupId": "ad751a6f-70c6-4cc5-acb3-491d33094aff",
  "userId": "bd2fe878-0300-40f1-9e90-76b252a15fdd",
  "payoutOrder": 3,
  "status": "ACTIVE",
  "joinDate": "2025-04-14T09:31:38.992678",
  "createdAt": null
}
```

#### Get Group Details
`GET /v1/groups/{groupId}`

Response:
```json
{
  "groupId": "78be0f06-5b76-4e6c-9465-5029f65b9b5c",
  "name": "Family Savings",
  "description": "Monthly family savings group",
  "creatorId": "user-1",
  "type": "ROTATING",
  "minimumContribution": 500,
  "minimumJoinAmount": 1000,
  "maxParticipants": 2,
  "contributionAmount": 500,
  "frequency": "MONTHLY",
  "payoutOrderType": "RANDOM",
  "status": "ACTIVE",
  "nextContributionDate": "2025-05-12T13:48:01.864191",
  "currentRound": 1,
  "targetAmount": null,
  "memberCount": 2,
  "createdAt": null
}
```

## Scheduled Jobs

The system includes a scheduled job that processes contributions automatically based on group frequency:

```java
@Scheduled(cron = "0 */1 * * * *") // Runs every minute
public void processScheduledContributions() {
    log.info("Processing scheduled contribution");
    LocalDateTime now = LocalDateTime.now();
    List<SavingGroup> dueGroups = groupRepository.findByNextContributionDateBeforeAndStatus(now, SavingGroup.GroupStatus.ACTIVE);

    dueGroups.forEach(group -> {
        try {
            processGroupContribution(group);
        } catch (Exception e) {
            log.error("Failed to process contributions for group {}: {}", group.getGroupId(), e.getMessage());
        }
    });
}
```

The contribution process:
1. Identifies all active groups with due contributions
2. Determines the current recipient based on group type and round
3. Transfers funds from all members to the recipient
4. Records the transactions
5. Updates the group for the next contribution cycle

## Configuration

API Gateway routing configuration (`application.yml`):

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/v1/user/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                securedEndpoints: /v1/auth/login,/v1/auth/register,/eureka,/v1/user/save,/v1/user/getUserByUsername

        - id: wallet-service
          uri: lb://wallet-service
          predicates:
            - Path=/v1/wallets/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                securedEndpoints: /v1/wallets,/eureka

        - id: saving-group-service
          uri: lb://saving-group-service
          predicates:
            - Path=/v1/groups/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                securedEndpoints: /eureka

        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/v1/auth/**

        - id: file-storage
          uri: lb://file-storage
          predicates:
            - Path=/v1/file-storage/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                securedEndpoints: /v1/auth/login,/v1/auth/register,/eureka
      httpclient:
        connect-timeout: 60000
        response-timeout: 60s
```

## Deployment

### Prerequisites
- Java 17+
- Docker
- Kubernetes (for production)
- Google Cloud Storage bucket (for file storage)
- PostgreSQL databases for each service

### Steps
1. Build all services: `mvn clean package`
2. Build Docker images: `docker-compose build`
3. Start services: `docker-compose up`
4. Access services via API Gateway on port 8003

## Error Handling

The system implements comprehensive error handling with appropriate HTTP status codes:

- 400 Bad Request: Invalid input data
- 401 Unauthorized: Missing or invalid authentication
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 500 Internal Server Error: Unexpected server error

Custom exceptions are defined for domain-specific errors:
- `GroupNameAlreadyExistException`
- `InsufficientJoinAmountException`
- `GroupNotFoundException`
- `InviteNotFoundException`
- `UnauthorizedGroupOperationException`
- `InviteExpiredException`
- `GroupNotAcceptingMembersException`
- `GroupFullException`
- `AlreadyGroupMemberException`
- `SelfInvitationException`

## Security

- JWT-based authentication
- Role-based access control
- Password encryption with BCrypt
- HTTPS for all communications
- Input validation
- Rate limiting (to be implemented)

## Testing

The system should be tested with:
- Unit tests for all service methods
- Integration tests for API endpoints
- End-to-end tests for complete workflows
- Load testing for performance evaluation
- Security testing for vulnerabilities

## Future Enhancements

1. **Notification Service**: Email/SMS alerts for contributions, payouts, etc.
2. **Mobile App**: Native mobile application
3. **Admin Dashboard**: For monitoring and management
4. **Interest Calculation**: For fixed deposit groups
5. **Early Withdrawal Penalties**: For fixed deposit groups
6. **Social Features**: Group chats, announcements
7. **Analytics**: Savings trends and reports
8. **Multi-currency Support**: For international users
9. **Webhooks**: For third-party integrations
10. **Escrow Service**: For added security of funds
