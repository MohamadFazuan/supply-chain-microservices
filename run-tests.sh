#!/bin/bash

# =============================================================================
# Supply Chain Microservices - Test Execution Script
# =============================================================================
# This script provides automated testing capabilities for the entire
# microservices architecture with proper setup and teardown.
# =============================================================================

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_COMPOSE_TEST_FILE="docker-compose.test.yml"
TEST_RESULTS_DIR="test-results"
COVERAGE_DIR="coverage"

# Test execution options
RUN_UNIT_TESTS=true
RUN_INTEGRATION_TESTS=false
RUN_COVERAGE=false
PARALLEL_EXECUTION=false
CLEAN_BUILD=false
MODULES=""
TEST_CLASSES=""
SKIP_INFRASTRUCTURE=false

# Infrastructure services
REQUIRED_SERVICES=("mysql" "redis" "rabbitmq")

# =============================================================================
# Utility Functions
# =============================================================================

print_banner() {
    echo -e "${BLUE}"
    echo "=================================================================="
    echo "  Supply Chain Microservices - Test Execution Suite"
    echo "=================================================================="
    echo -e "${NC}"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
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

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# =============================================================================
# Help Function
# =============================================================================

show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Supply Chain Microservices Test Execution Script

OPTIONS:
    -h, --help              Show this help message
    -u, --unit-only         Run only unit tests (default)
    -i, --integration       Run integration tests
    -a, --all               Run all tests (unit + integration)
    -c, --coverage          Generate coverage reports
    -p, --parallel          Run tests in parallel
    -C, --clean             Clean build before testing
    -m, --modules MODULE    Test specific modules (comma-separated)
    -t, --test-classes CLASS Test specific test classes (comma-separated)
    -s, --skip-infra        Skip infrastructure setup
    -v, --verbose           Verbose output

EXAMPLES:
    $0                                  # Run unit tests only
    $0 -a -c                           # Run all tests with coverage
    $0 -i -m auth-service,common-lib   # Run integration tests for specific modules
    $0 -u -t SecurityContextHolderTest # Run specific test class
    $0 -p -C                           # Clean build and run tests in parallel

MODULES:
    - common-lib
    - config-server
    - eureka-server
    - api-gateway
    - auth-service
    - product-service
    - credential-service
    - ai-service

EOF
}

# =============================================================================
# Environment Validation
# =============================================================================

check_prerequisites() {
    print_step "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | grep -oP 'version "?\K[0-9]+')
    if [ "$java_version" -lt 21 ]; then
        print_error "Java 21 or higher is required. Found Java $java_version"
        exit 1
    fi
    print_info "Java version: $java_version"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    print_info "Maven version: $(mvn -version | head -n1 | cut -d' ' -f3)"
    
    # Check Docker (only if integration tests)
    if [ "$RUN_INTEGRATION_TESTS" = true ] && [ "$SKIP_INFRASTRUCTURE" = false ]; then
        if ! command -v docker &> /dev/null; then
            print_error "Docker is not installed or not in PATH"
            exit 1
        fi
        
        if ! docker info &> /dev/null; then
            print_error "Docker is not running"
            exit 1
        fi
        print_info "Docker version: $(docker --version | cut -d' ' -f3 | tr -d ',')"
        
        if ! command -v docker-compose &> /dev/null; then
            print_error "Docker Compose is not installed or not in PATH"
            exit 1
        fi
    fi
    
    print_success "All prerequisites met"
}

# =============================================================================
# Infrastructure Management
# =============================================================================

start_test_infrastructure() {
    if [ "$SKIP_INFRASTRUCTURE" = true ]; then
        print_info "Skipping infrastructure setup"
        return 0
    fi
    
    print_step "Starting test infrastructure..."
    
    if [ ! -f "$DOCKER_COMPOSE_TEST_FILE" ]; then
        print_warning "Docker Compose test file not found, creating default..."
        create_test_docker_compose
    fi
    
    # Stop any existing containers
    docker-compose -f "$DOCKER_COMPOSE_TEST_FILE" down -v 2>/dev/null || true
    
    # Start infrastructure services
    docker-compose -f "$DOCKER_COMPOSE_TEST_FILE" up -d
    
    # Wait for services to be ready
    wait_for_services
    
    print_success "Test infrastructure is ready"
}

create_test_docker_compose() {
    cat > "$DOCKER_COMPOSE_TEST_FILE" << 'EOF'
version: '3.8'

services:
  mysql-test:
    image: mysql:8.0
    container_name: supply-chain-mysql-test
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: supply_chain_test
      MYSQL_USER: test_user
      MYSQL_PASSWORD: test_password
    ports:
      - "3307:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
    tmpfs:
      - /var/lib/mysql

  redis-test:
    image: redis:7-alpine
    container_name: supply-chain-redis-test
    ports:
      - "6380:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      timeout: 10s
      retries: 5

  rabbitmq-test:
    image: rabbitmq:3-management-alpine
    container_name: supply-chain-rabbitmq-test
    environment:
      RABBITMQ_DEFAULT_USER: test_user
      RABBITMQ_DEFAULT_PASS: test_password
    ports:
      - "5673:5672"
      - "15673:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      timeout: 30s
      retries: 10
EOF
    print_info "Created test Docker Compose file"
}

wait_for_services() {
    print_step "Waiting for services to be healthy..."
    
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local all_healthy=true
        
        for service in "${REQUIRED_SERVICES[@]}"; do
            local container_name="supply-chain-${service}-test"
            if ! docker ps --filter "name=$container_name" --filter "health=healthy" --format "table {{.Names}}" | grep -q "$container_name"; then
                all_healthy=false
                break
            fi
        done
        
        if [ "$all_healthy" = true ]; then
            print_success "All services are healthy"
            return 0
        fi
        
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    print_error "Services failed to become healthy within timeout"
    print_info "Checking service status..."
    docker-compose -f "$DOCKER_COMPOSE_TEST_FILE" ps
    exit 1
}

stop_test_infrastructure() {
    if [ "$SKIP_INFRASTRUCTURE" = true ]; then
        return 0
    fi
    
    print_step "Stopping test infrastructure..."
    docker-compose -f "$DOCKER_COMPOSE_TEST_FILE" down -v 2>/dev/null || true
    print_success "Test infrastructure stopped"
}

# =============================================================================
# Build Functions
# =============================================================================

clean_build() {
    if [ "$CLEAN_BUILD" = true ]; then
        print_step "Performing clean build..."
        mvn clean compile -DskipTests
        print_success "Clean build completed"
    fi
}

# =============================================================================
# Test Execution Functions
# =============================================================================

run_unit_tests() {
    if [ "$RUN_UNIT_TESTS" = false ]; then
        return 0
    fi
    
    print_step "Running unit tests..."
    
    local mvn_cmd="mvn test"
    
    # Add specific modules if specified
    if [ -n "$MODULES" ]; then
        mvn_cmd="$mvn_cmd -pl $MODULES"
    fi
    
    # Add specific test classes if specified
    if [ -n "$TEST_CLASSES" ]; then
        mvn_cmd="$mvn_cmd -Dtest=$TEST_CLASSES"
    fi
    
    # Add parallel execution if enabled
    if [ "$PARALLEL_EXECUTION" = true ]; then
        mvn_cmd="$mvn_cmd -T 4"
    fi
    
    # Add unit test group filter
    mvn_cmd="$mvn_cmd -Dgroups=unit"
    
    # Execute tests
    eval $mvn_cmd
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_success "Unit tests passed"
    else
        print_error "Unit tests failed"
        return $exit_code
    fi
}

run_integration_tests() {
    if [ "$RUN_INTEGRATION_TESTS" = false ]; then
        return 0
    fi
    
    print_step "Running integration tests..."
    
    # Set test environment variables
    export TEST_MYSQL_HOST=localhost
    export TEST_MYSQL_PORT=3307
    export TEST_REDIS_HOST=localhost
    export TEST_REDIS_PORT=6380
    export TEST_RABBITMQ_HOST=localhost
    export TEST_RABBITMQ_PORT=5673
    
    local mvn_cmd="mvn verify"
    
    # Add specific modules if specified
    if [ -n "$MODULES" ]; then
        mvn_cmd="$mvn_cmd -pl $MODULES"
    fi
    
    # Add integration test group filter
    mvn_cmd="$mvn_cmd -Dgroups=integration"
    
    # Execute tests
    eval $mvn_cmd
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_success "Integration tests passed"
    else
        print_error "Integration tests failed"
        return $exit_code
    fi
}

generate_coverage() {
    if [ "$RUN_COVERAGE" = false ]; then
        return 0
    fi
    
    print_step "Generating coverage reports..."
    
    # Generate JaCoCo reports
    mvn jacoco:report
    
    # Create coverage directory
    mkdir -p "$COVERAGE_DIR"
    
    # Copy coverage reports
    find . -name "jacoco.xml" -exec cp {} "$COVERAGE_DIR/" \;
    find . -path "*/site/jacoco/index.html" -exec cp -r "$(dirname {})" "$COVERAGE_DIR/" \;
    
    print_success "Coverage reports generated in $COVERAGE_DIR"
    
    # Display coverage summary
    if command -v xmllint &> /dev/null; then
        print_info "Coverage Summary:"
        find . -name "jacoco.xml" | while read -r file; do
            local module=$(dirname "$file" | sed 's|.*/||')
            local coverage=$(xmllint --xpath "string(//report/counter[@type='INSTRUCTION']/@covered)" "$file" 2>/dev/null || echo "N/A")
            local total=$(xmllint --xpath "string(//report/counter[@type='INSTRUCTION']/@missed)" "$file" 2>/dev/null || echo "N/A")
            if [ "$coverage" != "N/A" ] && [ "$total" != "N/A" ]; then
                local percentage=$(( coverage * 100 / (coverage + total) ))
                echo "  $module: ${percentage}%"
            fi
        done
    fi
}

# =============================================================================
# Report Generation
# =============================================================================

generate_test_report() {
    print_step "Generating test report..."
    
    mkdir -p "$TEST_RESULTS_DIR"
    
    # Collect test results
    find . -name "TEST-*.xml" -exec cp {} "$TEST_RESULTS_DIR/" \;
    
    # Generate summary
    cat > "$TEST_RESULTS_DIR/summary.txt" << EOF
Test Execution Summary
=====================
Date: $(date)
Unit Tests: $RUN_UNIT_TESTS
Integration Tests: $RUN_INTEGRATION_TESTS
Coverage: $RUN_COVERAGE
Modules: ${MODULES:-"All"}
Test Classes: ${TEST_CLASSES:-"All"}

Results:
--------
$(find "$TEST_RESULTS_DIR" -name "TEST-*.xml" | wc -l) test files generated
EOF
    
    print_success "Test report generated in $TEST_RESULTS_DIR"
}

# =============================================================================
# Cleanup Functions
# =============================================================================

cleanup() {
    print_step "Performing cleanup..."
    stop_test_infrastructure
    print_success "Cleanup completed"
}

# =============================================================================
# Signal Handlers
# =============================================================================

trap cleanup EXIT
trap 'print_error "Script interrupted"; exit 1' INT TERM

# =============================================================================
# Argument Parsing
# =============================================================================

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -u|--unit-only)
                RUN_UNIT_TESTS=true
                RUN_INTEGRATION_TESTS=false
                shift
                ;;
            -i|--integration)
                RUN_INTEGRATION_TESTS=true
                RUN_UNIT_TESTS=false
                shift
                ;;
            -a|--all)
                RUN_UNIT_TESTS=true
                RUN_INTEGRATION_TESTS=true
                shift
                ;;
            -c|--coverage)
                RUN_COVERAGE=true
                shift
                ;;
            -p|--parallel)
                PARALLEL_EXECUTION=true
                shift
                ;;
            -C|--clean)
                CLEAN_BUILD=true
                shift
                ;;
            -m|--modules)
                MODULES="$2"
                shift 2
                ;;
            -t|--test-classes)
                TEST_CLASSES="$2"
                shift 2
                ;;
            -s|--skip-infra)
                SKIP_INFRASTRUCTURE=true
                shift
                ;;
            -v|--verbose)
                set -x
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    print_banner
    
    parse_arguments "$@"
    
    # Validate environment
    check_prerequisites
    
    # Start infrastructure if needed
    if [ "$RUN_INTEGRATION_TESTS" = true ]; then
        start_test_infrastructure
    fi
    
    # Clean build if requested
    clean_build
    
    # Run tests
    local test_exit_code=0
    
    run_unit_tests || test_exit_code=$?
    
    if [ $test_exit_code -eq 0 ]; then
        run_integration_tests || test_exit_code=$?
    fi
    
    # Generate coverage regardless of test results
    generate_coverage
    
    # Generate test report
    generate_test_report
    
    # Final status
    if [ $test_exit_code -eq 0 ]; then
        print_success "All tests completed successfully!"
    else
        print_error "Some tests failed!"
        exit $test_exit_code
    fi
}

# Execute main function with all arguments
main "$@"
