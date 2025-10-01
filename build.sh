#!/bin/bash

# Supply Chain Microservices Build and Deploy Script
# Usage: ./build.sh [command]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2 | cut -d '.' -f 1)
    if [ "$java_version" -lt 21 ]; then
        print_error "Java 21 or higher is required. Current version: $java_version"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed"
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    
    print_success "All prerequisites are met"
}

# Build all modules
build_all() {
    print_status "Building all modules..."
    mvn clean install -DskipTests
    print_success "Build completed successfully"
}

# Build specific module
build_module() {
    local module=$1
    if [ -z "$module" ]; then
        print_error "Module name is required"
        exit 1
    fi
    
    print_status "Building module: $module"
    mvn clean install -pl "$module" -am -DskipTests
    print_success "Module $module built successfully"
}

# Run tests
run_tests() {
    print_status "Running tests..."
    mvn test
    print_success "All tests passed"
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    docker-compose up -d mysql redis rabbitmq portainer
    
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check MySQL
    if docker-compose exec -T mysql mysqladmin ping -h localhost -u root -prootpassword &> /dev/null; then
        print_success "MySQL is ready"
    else
        print_warning "MySQL might not be ready yet"
    fi
    
    # Check Redis
    if docker-compose exec -T redis redis-cli ping &> /dev/null; then
        print_success "Redis is ready"
    else
        print_warning "Redis might not be ready yet"
    fi
    
    print_success "Infrastructure services started"
}

# Start all services with Docker
start_docker() {
    print_status "Starting all services with Docker..."
    docker-compose up --build -d
    print_success "All services started with Docker"
}

# Start services locally
start_local() {
    print_status "Starting services locally..."
    
    # Start Config Server
    print_status "Starting Config Server..."
    cd config-server
    mvn spring-boot:run > ../logs/config-server.log 2>&1 &
    CONFIG_PID=$!
    cd ..
    sleep 30
    
    # Start Eureka Server
    print_status "Starting Eureka Server..."
    cd eureka-server
    mvn spring-boot:run > ../logs/eureka-server.log 2>&1 &
    EUREKA_PID=$!
    cd ..
    sleep 30
    
    # Start API Gateway
    print_status "Starting API Gateway..."
    cd api-gateway
    mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
    GATEWAY_PID=$!
    cd ..
    sleep 20
    
    # Start Auth Service
    print_status "Starting Auth Service..."
    cd auth-service
    mvn spring-boot:run > ../logs/auth-service.log 2>&1 &
    AUTH_PID=$!
    cd ..
    
    # Save PIDs for cleanup
    echo "$CONFIG_PID $EUREKA_PID $GATEWAY_PID $AUTH_PID" > .service_pids
    
    print_success "Services started locally"
    print_status "Log files are in ./logs/ directory"
    print_status "Service PIDs saved in .service_pids file"
}

# Stop all services
stop_all() {
    print_status "Stopping all services..."
    
    # Stop Docker services
    docker-compose down
    
    # Stop local services if PID file exists
    if [ -f .service_pids ]; then
        print_status "Stopping local services..."
        while read -r pid; do
            if ps -p $pid > /dev/null 2>&1; then
                kill $pid
            fi
        done < .service_pids
        rm -f .service_pids
    fi
    
    print_success "All services stopped"
}

# Clean up everything
clean_all() {
    print_status "Cleaning up everything..."
    
    # Stop services
    stop_all
    
    # Clean Maven artifacts
    mvn clean
    
    # Remove Docker volumes
    docker-compose down -v
    
    # Remove log files
    rm -rf logs/
    
    print_success "Cleanup completed"
}

# Check service status
status() {
    print_status "Checking service status..."
    
    # Check Docker services
    docker-compose ps
    
    echo ""
    print_status "Service URLs:"
    echo "- Eureka Dashboard: http://localhost:8761"
    echo "- API Gateway: http://localhost:8080"
    echo "- Config Server: http://localhost:8888"
    echo "- RabbitMQ Management: http://localhost:15672"
    echo "- Portainer: http://localhost:9000"
}

# Show logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        print_status "Showing all Docker logs..."
        docker-compose logs -f
    else
        print_status "Showing logs for $service..."
        if [ -f "logs/$service.log" ]; then
            tail -f "logs/$service.log"
        else
            docker-compose logs -f "$service"
        fi
    fi
}

# Create log directory
mkdir -p logs

# Main script logic
case "$1" in
    "check")
        check_prerequisites
        ;;
    "build")
        check_prerequisites
        build_all
        ;;
    "build-module")
        check_prerequisites
        build_module "$2"
        ;;
    "test")
        check_prerequisites
        run_tests
        ;;
    "start-infra")
        check_prerequisites
        start_infrastructure
        ;;
    "start-docker")
        check_prerequisites
        build_all
        start_docker
        ;;
    "start-local")
        check_prerequisites
        build_all
        start_infrastructure
        start_local
        ;;
    "stop")
        stop_all
        ;;
    "clean")
        clean_all
        ;;
    "status")
        status
        ;;
    "logs")
        show_logs "$2"
        ;;
    "dev")
        print_status "Starting development environment..."
        check_prerequisites
        build_all
        start_infrastructure
        start_local
        status
        ;;
    *)
        echo "Supply Chain Microservices Build Script"
        echo ""
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  check           - Check prerequisites"
        echo "  build           - Build all modules"
        echo "  build-module    - Build specific module (usage: build-module <module-name>)"
        echo "  test            - Run all tests"
        echo "  start-infra     - Start infrastructure services only"
        echo "  start-docker    - Start all services with Docker"
        echo "  start-local     - Start services locally"
        echo "  stop            - Stop all services"
        echo "  clean           - Clean up everything"
        echo "  status          - Check service status"
        echo "  logs            - Show logs (usage: logs [service-name])"
        echo "  dev             - Quick start for development"
        echo ""
        echo "Examples:"
        echo "  $0 dev                    # Quick start development environment"
        echo "  $0 build-module auth-service"
        echo "  $0 logs auth-service"
        echo "  $0 start-docker"
        ;;
esac
