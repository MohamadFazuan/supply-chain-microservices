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

┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│     MySQL       │ │      Redis      │ │    RabbitMQ     │ │   Portainer     │
│   (Database)    │ │    (Cache)      │ │   (Messaging)   │ │  (Monitoring)   │
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 🚀 Features

### Core Services
- **Config Server**: Centralized configuration management with Spring Cloud Config
- **Eureka Server**: Service discovery and registration
- **API Gateway**: Request routing, authentication, rate limiting, and load balancing
- **Auth Service**: JWT-based authentication and authorization with role-based access control
- **Product Service**: Product management with Redis caching and RabbitMQ messaging
- **Credential Service**: Secure API key and secret management using Vault pattern and AES-GCM encryption
- **AI Service**: Multi-provider AI integration with OpenAI, Azure OpenAI, and Ollama support

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
- **Circuit Breakers** with Resilience4j
- **Distributed Tracing** with Spring Cloud Sleuth
- **Health Checks** and monitoring endpoints
- **Rate Limiting** and throttling
- **JWT Token Management** with refresh tokens
- **Advanced Encryption** with AES-GCM and BouncyCastle
- **AI Provider Abstraction** with fallback mechanisms
- **Database Migration** with Flyway
- **Containerized Deployment** with multi-stage builds
- **Security Best Practices** with OWASP compliance

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

## 🛠️ Getting Started

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

## 🔍 Monitoring and Observability

### Health Checks
All services expose actuator endpoints:
```bash
# Service health
GET /actuator/health

# Service info
GET /actuator/info

# Metrics
GET /actuator/metrics

# Prometheus metrics
GET /actuator/prometheus
```

### Logging
- **Structured Logging** with JSON format
- **Centralized Logging** with ELK Stack (optional)
- **Log Correlation** with trace IDs
- **Performance Metrics** with Micrometer

## 🚧 Development Guidelines

### Adding New Service
1. Create new module in parent POM
2. Extend from parent dependencies
3. Implement Eureka client configuration
4. Add Spring Cloud Config integration
5. Include health checks and metrics
6. Create Dockerfile and add to compose.yaml

### Database Migrations
```bash
# Create new migration
src/main/resources/db/migration/V1__Create_users_table.sql

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
