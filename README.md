# Supply Chain Logistics Microservices Architecture

A comprehensive microservices architecture built with **Java 21**, **Spring Boot 3**, and **Spring Cloud** for managing supply chain logistics operations. This project demonstrates enterprise-grade patterns, containerization, and production-ready practices.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Mobile App    │    │  External APIs  │
│   (React/Vue)   │    │   (React Native)│    │   (Partners)    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │      API Gateway         │
                    │  (Spring Cloud Gateway)  │
                    │   - Routing              │
                    │   - Authentication       │
                    │   - Rate Limiting        │
                    │   - Load Balancing       │
                    └─────────────┬─────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼─────────┐ ┌─────────▼─────────┐ ┌─────────▼─────────┐
    │   Auth Service    │ │  Product Service  │ │ Credential Service│
    │   - JWT Tokens    │ │   - Inventory     │ │  - API Keys       │
    │   - User Mgmt     │ │   - Caching       │ │  - Secrets        │
    │   - Roles/Perms   │ │   - Events        │ │  - Encryption     │
    └─────────┬─────────┘ └─────────┬─────────┘ └─────────┬─────────┘
              │                     │                     │
              └─────────────────────┼─────────────────────┘
                                   │
                    ┌─────────────▼─────────────┐
                    │   Service Discovery      │
                    │     (Eureka Server)      │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │   Config Server          │
                    │ (Centralized Config)     │
                    └──────────────────────────┘

```
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│     MySQL       │ │      Redis      │ │    RabbitMQ     │ │   Portainer     │
│   (Database)    │ │    (Cache)      │ │   (Messaging)   │ │  (Monitoring)   │
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 📑 Table of Contents

- [🚀 Features](#-features)
- [📋 Prerequisites](#-prerequisites)
- [⚡ Quick Start](#-quick-start)
- [🛠️ Detailed Setup](#️-detailed-setup)
- [🗄️ Database Schema & Management](#️-database-schema--management)
- [📊 Logging & Monitoring](#-logging--monitoring)
- [🔐 Security Implementation](#-security-implementation)
- [📡 API Documentation](#-api-documentation)
- [🔍 Enhanced Monitoring and Observability](#-enhanced-monitoring-and-observability)
- [🚧 Development Guidelines](#-development-guidelines)
- [🔧 Troubleshooting](#-troubleshooting)
- [📚 Additional Documentation](#-additional-documentation)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
```

## 🚀 Features

### Core Services
- **Config Server**: Centralized configuration management with Spring Cloud Config
- **Eureka Server**: Service discovery and registration
- **API Gateway**: Request routing, authentication, rate limiting, and load balancing with circuit breakers
- **Auth Service**: JWT-based authentication and authorization with role-based access control and comprehensive user management
- **Product Service**: Complete logistics product management with inventory tracking, pricing models, and warehouse operations
- **Credential Service**: Secure API key and secret management using Vault pattern and AES-GCM encryption with audit logging
- **AI Service**: Multi-provider AI integration with request/response tracking, usage analytics, and feedback collection
- **Common Library**: Shared security, logging, and monitoring components across all services

### Technology Stack
- **Java 21** with Virtual Threads support
- **Spring Boot 3.3.4** with latest features
- **Spring Cloud 2023.0.3** for microservices patterns
- **Spring Security 6** for comprehensive security
- **MySQL 8** for persistent data storage
- **Redis 7** for caching and session management
- **RabbitMQ 3** for asynchronous messaging
- **Ollama** for local AI model inference
- **Docker & Docker Compose** for containerization
- **Portainer** for container monitoring and management

### Production Features
- **Multiple Design Patterns**: Strategy, Factory, Chain of Responsibility, Observer, Facade, Repository
- **Circuit Breakers** with Resilience4j for fault tolerance
- **Distributed Tracing** with Spring Cloud Sleuth and Zipkin integration
- **Comprehensive Monitoring** with Prometheus, Grafana, and custom metrics
- **Centralized Logging** with structured patterns, correlation IDs, and ELK stack support
- **Database Schema Management** with Flyway migrations for all services
- **Security Audit Trails** with complete request/response logging
- **Performance Monitoring** with method execution timing and database query tracking
- **Rate Limiting** and throttling with Redis-based implementation
- **JWT Token Management** with refresh tokens and security validation
- **Advanced Encryption** with AES-GCM and BouncyCastle for sensitive data
- **AI Provider Abstraction** with fallback mechanisms and usage analytics
- **Container Orchestration** with multi-stage Docker builds and health checks
- **Business Metrics Tracking** with custom monitoring for supply chain operations
- **Security Best Practices** with OWASP compliance and comprehensive audit logging

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

## ⚡ Quick Start

```bash
# 1. Clone and build
git clone <repository-url>
cd supply-chain-microservices
mvn clean install

# 2. Start infrastructure
docker-compose up -d mysql redis rabbitmq

# 3. Start all services
docker-compose up --build

# 4. Verify deployment
curl http://localhost:8761  # Eureka Dashboard
curl http://localhost:8080/health  # API Gateway Health
```

🎉 **That's it!** Your supply chain microservices are now running with:
- **Full database schemas** automatically created via Flyway
- **Comprehensive monitoring** with Prometheus metrics
- **Structured logging** with correlation IDs
- **Security** with JWT authentication
- **Service discovery** and load balancing

## 🛠️ Detailed Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd demo
```

### 2. Build the Project
```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl config-server
```

### 3. Start Infrastructure Services
```bash
# Start all infrastructure services
docker-compose up -d mysql redis rabbitmq portainer

# Wait for services to be healthy
docker-compose ps
```

### 4. Start Microservices

#### Option A: Using Docker Compose (Recommended)
```bash
# Build and start all services
docker-compose up --build

# Start specific services
docker-compose up --build config-server eureka-server
```

#### Option B: Running Locally
```bash
# Terminal 1: Config Server
cd config-server
mvn spring-boot:run

# Terminal 2: Eureka Server (wait for config server)
cd eureka-server
mvn spring-boot:run

# Terminal 3: API Gateway (wait for eureka)
cd api-gateway
mvn spring-boot:run

# Terminal 4: Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 5: Product Service
cd product-service
mvn spring-boot:run
```

### 5. Verify Deployment
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888
- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)
- **Portainer**: http://localhost:9000

## 🔧 Configuration

### Environment Variables
```bash
# Database
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USERNAME=supply_user
MYSQL_PASSWORD=supply_pass

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin123

# JWT
JWT_SECRET=myVerySecretKeyForJWTTokenGenerationAndValidation123456789
JWT_EXPIRATION=86400
```

### Service Ports
| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Main entry point |
| Auth Service | 8081 | Authentication |
| Product Service | 8082 | Product management |
| Credential Service | 8083 | Credential management |
| AI Service | 8084 | AI processing |
| Config Server | 8888 | Configuration |
| Eureka Server | 8761 | Service discovery |
| MySQL | 3306 | Database |
| Redis | 6379 | Cache |
| RabbitMQ | 5672/15672 | Messaging |
| Ollama | 11434 | Local AI models |
| Portainer | 9000 | Monitoring |

## 🗄️ Database Schema & Management

### Database Architecture
Our microservices use a **database-per-service** pattern with comprehensive Flyway migrations for schema management:

#### **Auth Service Database**
- `users` - User accounts with authentication fields
- `roles` - System roles (ADMIN, MANAGER, USER, SUPPLIER, CUSTOMER, WAREHOUSE_STAFF)
- `user_roles` - Many-to-many relationship between users and roles

#### **Product Service Database**
- `product_categories` - Hierarchical product categorization
- `products` - Core logistics products (warehouse storage, freight forwarding, transportation)
- `product_inventory` - Multi-location stock management
- `product_pricing` - Flexible pricing models (fixed, tiered, quote-based)
- `logistics_packages` - Bundled service packages
- `freight_forwarding_routes` - Shipping route management
- `transportation_fleet` - Fleet tracking and management
- `warehouse_storage_units` - Storage unit specifications

#### **Credential Service Database**
- `credentials` - Encrypted API keys and secrets storage
- `credential_access_log` - Security audit trail for credential access

#### **AI Service Database**
- `ai_requests` - AI service request logging
- `ai_responses` - AI response tracking with performance metrics
- `ai_usage_metrics` - Analytics and usage statistics
- `ai_feedback` - User feedback for model improvement

### Database Features
- **Flyway Migrations**: Version-controlled schema evolution
- **Comprehensive Indexing**: Optimized for query performance
- **Audit Trails**: Created/updated timestamps and user tracking
- **Data Integrity**: Foreign key constraints and validation
- **JSON Support**: Flexible metadata storage
- **UTF-8 Unicode**: International character support

### Migration Management
```bash
# Run migrations for specific service
cd product-service
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Clean database (development only)
mvn flyway:clean
```

## 📊 Logging & Monitoring

### Comprehensive Observability Stack
Our microservices include enterprise-grade logging and monitoring capabilities:

#### **Logging Features**
- **Structured Logging** with JSON format and correlation IDs
- **Request/Response Tracing** with unique request IDs
- **Performance Monitoring** with method execution timing
- **Security Audit Trails** with complete user action logging
- **Distributed Tracing** with Spring Cloud Sleuth integration
- **Centralized Log Management** with ELK stack support

#### **Monitoring Infrastructure**
- **Prometheus** - Metrics collection and alerting
- **Grafana** - Real-time dashboards and visualization
- **Custom Metrics Service** - Business-specific metrics tracking
- **Actuator Endpoints** - Health checks and operational metrics
- **Circuit Breaker Monitoring** - Resilience4j integration

#### **Key Metrics Tracked**
- **Business Metrics**: Product creation rates, order processing times, inventory changes
- **Technical Metrics**: Response times, error rates, database performance
- **Security Metrics**: Authentication attempts, failed logins, security events
- **AI Metrics**: Request/response times, token usage, provider performance

### Monitoring Setup
```bash
# Start monitoring stack
docker-compose -f monitoring-stack.yaml up -d

# Access monitoring tools
http://localhost:3000     # Grafana (admin/admin123)
http://localhost:9090     # Prometheus
http://localhost:5601     # Kibana
http://localhost:9411     # Zipkin Tracing
```

### Log Analysis Examples
```bash
# View logs with correlation
grep "abc123" logs/*.log

# Monitor specific service
tail -f logs/product-service.log | grep "PERFORMANCE"

# Check error patterns
grep "ERROR" logs/*.log | grep -c "database"
```

## 🔐 Security Implementation

### JWT Authentication Flow
1. User authenticates via `/api/auth/login`
2. Auth service validates credentials and issues JWT
3. Client includes JWT in Authorization header
4. API Gateway validates JWT and forwards user info
5. Services receive user context in headers

### Security Features
- **Password Hashing** with BCrypt
- **JWT Token Validation** at gateway level
- **Role-based Access Control** (RBAC)
- **Service-to-Service Authentication**
- **CORS Configuration** for cross-origin requests
- **Rate Limiting** to prevent abuse

## 📡 API Documentation

### Authentication Endpoints
```bash
# Register new user
POST /api/auth/register
Content-Type: application/json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"]
}

# Login
POST /api/auth/login
Content-Type: application/json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}

# Response
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "refreshToken": "refresh_token_here",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"],
  "expiresAt": "2024-10-02T10:30:00"
}
```

### Product Endpoints
```bash
# Get all products (with caching)
GET /api/products
Authorization: Bearer {jwt_token}

# Create product
POST /api/products
Authorization: Bearer {jwt_token}
Content-Type: application/json
{
  "name": "Product Name",
  "description": "Product Description",
  "price": 99.99,
  "quantity": 100,
  "category": "Electronics"
}
```

## 🐳 Docker Deployment

### Build Images
```bash
# Build all service images
mvn clean package
docker-compose build

# Build specific service
docker-compose build auth-service
```

### Production Deployment
```bash
# Production environment
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Scale services
docker-compose up -d --scale product-service=3
```

### Monitoring with Portainer
1. Access Portainer at http://localhost:9000
2. Create admin account
3. Monitor containers, networks, and volumes
4. View logs and performance metrics

## 🔍 Enhanced Monitoring and Observability

### Comprehensive Health Monitoring
All services expose enhanced actuator endpoints with business-specific health indicators:
```bash
# Complete service health with dependencies
GET /actuator/health

# Service information and build details
GET /actuator/info

# Custom business metrics
GET /actuator/metrics

# Prometheus metrics for monitoring stack
GET /actuator/prometheus

# Database migration status
GET /actuator/flyway

# Cache statistics
GET /actuator/caches

# HTTP request traces
GET /actuator/httptrace
```

### Advanced Logging Capabilities
- **Structured JSON Logging** with correlation IDs and user context
- **Distributed Tracing** with Spring Cloud Sleuth and Zipkin
- **Request/Response Logging** with execution time tracking
- **Security Audit Trails** with comprehensive user action logging
- **Performance Monitoring** with method-level execution timing
- **Error Tracking** with stack traces and resolution notes
- **Business Event Logging** for supply chain operations

### Custom Metrics and Analytics
```bash
# Business metrics examples
supply.chain.products.created
supply.chain.orders.processed{type="FREIGHT"}
supply.chain.auth.attempts{status="success"}
supply.chain.ai.requests{provider="OPENAI",type="DEMAND_FORECASTING"}

# Performance metrics
supply.chain.database.query.time{operation="FIND_PRODUCT"}
supply.chain.external.calls.time{service="PAYMENT_GATEWAY"}

# Security metrics
supply.chain.security.events{type="SUSPICIOUS_LOGIN",severity="HIGH"}
```

### Monitoring Stack Integration
- **Prometheus** integration for metrics collection
- **Grafana** dashboards for real-time visualization
- **ELK Stack** support for centralized log management
- **Custom alerting** for business and technical events

## 🚧 Development Guidelines

### Adding New Service
1. Create new module in parent POM
2. Extend from parent dependencies
3. Implement Eureka client configuration
4. Add Spring Cloud Config integration
5. Include comprehensive health checks and custom metrics
6. Add Flyway migrations for database schema
7. Implement logging aspects and monitoring
8. Add security audit trails
9. Create Dockerfile and add to compose.yaml
10. Include service in monitoring stack configuration

### Database Management Best Practices
```bash
# Create new migration with proper naming
src/main/resources/db/migration/V1__Initial_Service_Schema.sql
src/main/resources/db/migration/V2__Add_Audit_Tables.sql

# Run migrations
mvn flyway:migrate
```

### Testing Strategy
- **Unit Tests** with JUnit 5 and Mockito
- **Integration Tests** with TestContainers
- **Contract Testing** with Spring Cloud Contract
- **Load Testing** with JMeter or Gatling

## 🔧 Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   ```bash
   # Check Eureka dashboard
   curl http://localhost:8761/
   
   # Check service registration
   curl http://localhost:8761/eureka/apps
   ```

2. **Database Connection Issues**
   ```bash
   # Check MySQL connectivity
   docker exec -it supply-chain-mysql mysql -u supply_user -p
   
   # View logs
   docker logs supply-chain-mysql
   ```

3. **JWT Token Issues**
   ```bash
   # Validate token structure
   echo "token_here" | base64 -d
   
   # Check auth service logs
   docker logs supply-chain-auth-service
   ```

## 📈 Performance Optimization

### Caching Strategy
- **Redis** for session and application data
- **HTTP Caching** headers for static content
- **Database Query Optimization** with indexes
- **Connection Pooling** with HikariCP

### Scaling Considerations
- **Horizontal Scaling** with multiple instances
- **Load Balancing** via API Gateway
- **Database Sharding** for large datasets
- **Message Queue Partitioning** for high throughput

## 📚 Additional Documentation

This project includes comprehensive documentation for all aspects of the supply chain microservices:

### **📊 Database Documentation**
- **`DATABASE_SCHEMA_SUMMARY.md`** - Complete database schema overview for all services
- **`AUDIT_LOGGING_TABLES.sql`** - Enhanced audit tables for comprehensive logging
- **Flyway Migration Files** - Version-controlled database schema evolution
  - `auth-service/src/main/resources/db/migration/V1__Initial_Auth_Schema.sql`
  - `product-service/src/main/resources/db/migration/V*__*.sql` 
  - `credential-service/src/main/resources/db/migration/V1__Initial_Credential_Schema.sql`
  - `ai-service/src/main/resources/db/migration/V1__Initial_AI_Schema.sql`

### **📈 Monitoring & Logging Documentation**
- **`LOGGING_MONITORING_GUIDE.md`** - Complete implementation guide for logging and monitoring
- **`ENHANCED_LOGGING_CONFIG.yml`** - Enhanced centralized logging configuration
- **`monitoring-stack.yaml`** - Complete monitoring infrastructure setup
- **`monitoring/prometheus/prometheus.yml`** - Prometheus configuration for all services

### **🏗️ Infrastructure Documentation**
- **`compose.yaml`** - Main application stack
- **`monitoring-stack.yaml`** - Monitoring infrastructure (Prometheus, Grafana, ELK, Zipkin)
- **Service-specific Dockerfiles** - Optimized container builds for each service

### **🔧 Configuration Files**
- **`config-server/src/main/resources/config-repo/`** - Centralized configuration for all services
- **Service-specific `application.yml`** files with comprehensive settings
- **Environment-specific profiles** (dev, test, prod)

### **🛠️ Development Resources**
- **Common Library Components** - Shared logging, security, and monitoring utilities
- **Custom Annotations** - `@LogExecutionTime` for performance monitoring
- **Aspect-Oriented Programming** - Logging and security aspects
- **Custom Metrics Service** - Business-specific metrics tracking

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review service logs with `docker logs <service-name>`

---

**Built with ❤️ for Supply Chain Excellence**

name: Supply Chain Microservices CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: docker.io
  IMAGE_PREFIX: supply-chain

jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: test_db
          MYSQL_USER: test_user
          MYSQL_PASSWORD: test_pass
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven

    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Run unit tests
      run: mvn clean test -B

    - name: Run integration tests
      run: mvn clean verify -P integration-tests -B
      env:
        SPRING_PROFILES_ACTIVE: test
        MYSQL_URL: jdbc:mysql://localhost:3306/test_db
        MYSQL_USERNAME: test_user
        MYSQL_PASSWORD: test_pass
        REDIS_HOST: localhost
        REDIS_PORT: 6379

    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: '**/target/surefire-reports/*.xml'
        reporter: java-junit

    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml

  security-scan:
    name: Security Scan
    runs-on: ubuntu-latest
    needs: test
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: OWASP Dependency Check
      run: |
        mvn org.owasp:dependency-check-maven:check
    
    - name: Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'

    - name: Upload Trivy scan results
      uses: github/codeql-action/upload-sarif@v2
      if: always()
      with:
        sarif_file: 'trivy-results.sarif'

  build-and-push:
    name: Build and Push Images
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/main'
    
    strategy:
      matrix:
        service:
          - config-server
          - eureka-server
          - api-gateway
          - auth-service
          - product-service
          - credential-service
          - ai-service

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven

    - name: Build application
      run: mvn clean package -DskipTests -B

    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3

    - name: Log in to Docker Hub
      uses: docker/login-action@v3
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}

    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_PREFIX }}-${{ matrix.service }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}

    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: ./${{ matrix.service }}
        platforms: linux/amd64,linux/arm64
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    needs: build-and-push
    environment: staging
    if: github.ref == 'refs/heads/main'

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment..."
        # Add your staging deployment commands here
        # This could be kubectl, docker-compose, or other deployment tools

  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment: production
    if: github.ref == 'refs/heads/main'

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Deploy to production
      run: |
        echo "Deploying to production environment..."
        # Add your production deployment commands here
