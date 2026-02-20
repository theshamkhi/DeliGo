# 📦 DeliGo - Smart Delivery Management System

> Modern logistics management system for SmartLogi - Complete package delivery management across Morocco with JWT authentication and role-based access control

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-purple.svg)](https://www.postgresql.org/)
[![Security](https://img.shields.io/badge/Security-JWT-yellow.svg)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

---

## 📖 Overview

**DeliGo** is a comprehensive web application designed to modernize and automate SmartLogi's delivery operations. The system replaces manual Excel-based management with a complete package tracking and traceability solution, featuring enterprise-grade security with JWT authentication and role-based access control.

### Business Context

SmartLogi uses DeliGo to manage the entire logistics cycle:
- Package collection
- Warehouse storage
- Delivery operations
- Real-time package tracking
- Client and delivery personnel management

---

## ✨ Features

### Core Features

#### 📦 Package Management
- Create, track, and update package status
- Multi-criteria filtering (status, priority, zone, delivery person)
- Complete delivery history tracking
- Product management per package
- Overdue package alerts
- Statistical dashboards

#### 👥 User Management
- **Client Senders**: Create delivery requests and track their packages
- **Recipients**: Package destination management
- **Delivery Personnel**: Assignment and route tracking
- Role-based access control (RBAC)

#### 🗺️ Geographic Zones
- Zone organization for delivery optimization
- Automatic delivery personnel assignment by zone
- Coverage area management

#### 🔐 Security Features
- JWT-based stateless authentication
- Three roles: MANAGER, LIVREUR (Delivery), CLIENT
- Fine-grained permission system
- CORS protection
- BCrypt password encryption
- Token expiration management (24 hours)

#### 📊 Analytics & Reports
- Statistics by delivery person
- Statistics by zone
- Package count and total weight
- Performance dashboards
- Search and advanced filtering

#### 🔍 Advanced Search
- Keyword search across multiple fields
- Multi-criteria filtering
- Pagination support
- Sorting options

---

## 🛠️ Technologies

### Backend Stack
- **Java 17** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **Spring Security 6.x** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Spring Validation** - Data validation
- **JJWT 0.12.3** - JWT token management
- **PostgreSQL 15** - Relational database
- **Liquibase** - Database migrations
- **MapStruct 1.5.5** - Entity-DTO mapping
- **Lombok** - Boilerplate reduction
- **Maven** - Dependency management

### Documentation & API
- **Swagger/OpenAPI 3** - Interactive API documentation
- **Spring REST** - RESTful API design

### DevOps & Quality
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **JaCoCo** - Code coverage (90% target)
- **SonarQube** - Code quality analysis
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework

---

## 📸 Screenshots

### Swagger UI - Interactive API Documentation

<img width="1896" height="902" alt="Screenshot 2025-12-17 181328" src="https://github.com/user-attachments/assets/f35512c7-e6fc-4acd-b803-197bd6bdc108" />
*Complete API documentation with authentication support*

### UML Class Diagram

![UML Diagram](uml/Class.png)
*Entity relationship diagram showing the domain model*

### JaCoCo Code Coverage Report

<img width="1919" height="906" alt="Screenshot 2025-12-17 190208" src="https://github.com/user-attachments/assets/fc2193e4-cd03-4825-814f-23355e7b43f6" />

*Test coverage report - Target: 60%+*

### SonarQube Quality Analysis

<img width="1919" height="906" alt="Screenshot 2025-12-17 195330" src="https://github.com/user-attachments/assets/761e9c63-6284-445c-a3ad-530c3db4fc9e" />

*Code quality metrics and technical debt analysis*

---

## 🏗️ Architecture

The project follows **Domain-Driven Design (DDD)** principles with clean architecture:

```
src/main/java/com/shamkhi/deligo/
├── domain/                      # Business Logic Layer
│   ├── security/               # Security domain
│   │   ├── model/             # User, Role, Permission entities
│   │   ├── dto/               # Security DTOs
│   │   ├── repository/        # Security repositories
│   │   └── service/           # Authentication & authorization
│   ├── colis/                 # Package domain
│   │   ├── model/             # Package, History entities
│   │   ├── dto/               # Package DTOs
│   │   ├── repository/        # Package repositories
│   │   └── service/           # Package business logic
│   ├── client/                # Client domain
│   │   ├── model/             # Sender, Recipient entities
│   │   └── service/           # Client management
│   ├── livraison/             # Delivery domain
│   │   ├── model/             # Delivery person, Zone entities
│   │   └── service/           # Delivery management
│   └── produit/               # Product domain
│       └── service/           # Product management
│
├── application/                # Application Layer
│   ├── controller/            # REST Controllers
│   │   ├── AuthController     # Authentication endpoints
│   │   ├── AdminController    # Admin management
│   │   ├── ColisController    # Package endpoints
│   │   └── ...                # Other controllers
│   ├── mapper/                # MapStruct mappers
│   │   ├── SecurityMapper     # Security entity-DTO mapping
│   │   ├── ColisMapper        # Package mapping
│   │   └── ...                # Other mappers
│   ├── security/              # Security configuration
│   │   ├── SecurityConfig     # Spring Security config
│   │   ├── JwtAuthFilter      # JWT validation filter
│   │   └── UserDetailsService # User authentication
│   └── config/                # Application configuration
│       ├── WebConfig          # CORS configuration
│       └── OpenApiConfig      # Swagger configuration
│
└── infrastructure/             # Infrastructure Layer
    └── exception/             # Global exception handling
        ├── GlobalExceptionHandler
        ├── ResourceNotFoundException
        └── DuplicateResourceException
```
---

## 🔐 Security

### Authentication & Authorization

DeliGo implements enterprise-grade security with JWT (JSON Web Tokens):

#### Authentication Flow
```
1. User logs in with credentials
2. Server validates and generates JWT token
3. Client stores token (localStorage/sessionStorage)
4. Client includes token in Authorization header
5. Server validates token on each request
6. Request processed or rejected based on token validity
```

#### Role-Based Access Control (RBAC)

**ROLE_MANAGER** (Administrator)
- Full system access
- User, delivery personnel, and zone management
- Access to all packages and statistics
- Permission management

**ROLE_LIVREUR** (Delivery Personnel)
- View assigned packages only
- Update status of assigned packages
- No access to other delivery personnel data
- Limited to own delivery operations

**ROLE_CLIENT** (Client Sender)
- Create delivery requests
- View own packages only
- Track package status
- No access to delivery personnel or zones

#### Permission System

Nine fine-grained permissions:
- `READ_COLIS` - View packages
- `WRITE_COLIS` - Create packages
- `UPDATE_COLIS` - Modify packages
- `DELETE_COLIS` - Delete packages
- `UPDATE_STATUT_COLIS` - Change package status
- `MANAGE_LIVREURS` - Manage delivery personnel
- `MANAGE_ZONES` - Manage zones
- `MANAGE_CLIENTS` - Manage clients
- `VIEW_STATISTICS` - Access statistics

#### Security Features

✅ Stateless authentication (JWT)  
✅ BCrypt password encryption  
✅ Token expiration (24 hours)  
✅ CORS protection  
✅ Role-based endpoint protection  
✅ Custom access denied handling  
✅ SQL injection prevention (JPA)  
✅ XSS protection

---

## 🚀 Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Docker (optional)

### Option 1: Local Installation

#### 1. Clone the repository
```bash
git clone https://github.com/theshamkhi/DeliGo.git
cd DeliGo
```

#### 2. Create the database
```sql
CREATE DATABASE DeliGo;
```

#### 3. Configure application.yaml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/DeliGo
    username: postgres
    password: your_password

jwt:
  secret: your_secure_secret_key_here_change_in_production
  expiration: 86400000  # 24 hours
```

#### 4. Build and run
```bash
# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

#### 5. Access the application
- **API Base URL**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v1/api-docs

### Option 2: Docker Installation

#### 1. Using Docker Compose (Recommended)
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```
---

## 🧪 Testing & Quality

### Code Coverage with JaCoCo

The project maintains **90%+ code coverage** with JaCoCo:

```bash
# Run tests with coverage
mvn clean test

# Generate coverage report
mvn jacoco:report

# View report
open target/site/jacoco/index.html
```

**Coverage Configuration:**
- Minimum line coverage: 60%
- Excluded: Config, DTOs, Models, Generated code

### Code Quality with SonarQube

Integrated with SonarQube for continuous quality analysis:

```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=deligo \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=<your-token>
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ColisServiceTest

# Run tests with coverage
mvn clean verify

# Skip tests
mvn clean install -DskipTests
```