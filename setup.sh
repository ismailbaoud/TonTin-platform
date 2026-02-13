#!/bin/bash

# TonTin Platform - Setup Script
# This script helps you get started with the TonTin Platform using Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_header() {
    echo -e "\n${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}\n"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verify prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    local all_good=true

    # Check Docker
    if command_exists docker; then
        DOCKER_VERSION=$(docker --version)
        print_success "Docker is installed: $DOCKER_VERSION"
    else
        print_error "Docker is not installed"
        echo "  Please install Docker from: https://docs.docker.com/get-docker/"
        all_good=false
    fi

    # Check Docker Compose
    if command_exists docker-compose; then
        COMPOSE_VERSION=$(docker-compose --version)
        print_success "Docker Compose is installed: $COMPOSE_VERSION"
    else
        print_error "Docker Compose is not installed"
        echo "  Please install Docker Compose from: https://docs.docker.com/compose/install/"
        all_good=false
    fi

    # Check if Docker daemon is running
    if docker info >/dev/null 2>&1; then
        print_success "Docker daemon is running"
    else
        print_error "Docker daemon is not running"
        echo "  Please start Docker and try again"
        all_good=false
    fi

    if [ "$all_good" = false ]; then
        print_error "Please install missing prerequisites and try again"
        exit 1
    fi

    print_success "All prerequisites met!"
}

# Setup environment file
setup_environment() {
    print_header "Setting Up Environment"

    if [ -f .env ]; then
        print_warning ".env file already exists"
        read -p "Do you want to overwrite it? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Keeping existing .env file"
            return
        fi
    fi

    cp env.example .env
    print_success "Created .env file from env.example"

    print_info "Please configure your email settings in .env file"
    print_warning "You need to set MAIL_USERNAME and MAIL_PASSWORD for email functionality"
    echo ""

    read -p "Do you want to configure email settings now? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        read -p "Enter your Gmail address: " email
        read -s -p "Enter your Gmail App Password: " password
        echo

        # Update .env file
        sed -i.bak "s/MAIL_USERNAME=.*/MAIL_USERNAME=$email/" .env
        sed -i.bak "s/MAIL_PASSWORD=.*/MAIL_PASSWORD=$password/" .env
        rm .env.bak

        print_success "Email configuration updated"
    else
        print_warning "Remember to update email settings in .env before using email features"
    fi
}

# Build Docker images
build_images() {
    print_header "Building Docker Images"

    print_info "This may take several minutes on first run..."

    if docker-compose build; then
        print_success "Docker images built successfully"
    else
        print_error "Failed to build Docker images"
        exit 1
    fi
}

# Start services
start_services() {
    print_header "Starting Services"

    print_info "Starting all services in detached mode..."

    if docker-compose up -d; then
        print_success "Services started successfully"
    else
        print_error "Failed to start services"
        exit 1
    fi

    print_info "Waiting for services to be healthy..."
    sleep 10

    # Check service health
    check_services
}

# Check service health
check_services() {
    print_header "Checking Service Health"

    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))

        # Check backend
        if curl -s http://localhost:9090/actuator/health >/dev/null 2>&1; then
            print_success "Backend is healthy"
            break
        else
            if [ $attempt -eq $max_attempts ]; then
                print_error "Backend health check failed"
                print_info "Check logs with: docker-compose logs platform-back"
            else
                echo -n "."
                sleep 2
            fi
        fi
    done

    # Check frontend
    if curl -s http://localhost >/dev/null 2>&1; then
        print_success "Frontend is healthy"
    else
        print_warning "Frontend might still be starting up"
    fi

    # Check database
    if docker exec tontin-postgres pg_isready -U happy >/dev/null 2>&1; then
        print_success "Database is healthy"
    else
        print_error "Database is not responding"
    fi
}

# Show access information
show_access_info() {
    print_header "Access Information"

    echo -e "${GREEN}✓ Setup Complete!${NC}\n"
    echo "You can now access the application at:"
    echo ""
    echo -e "  ${BLUE}Frontend:${NC}    http://localhost"
    echo -e "  ${BLUE}Backend API:${NC} http://localhost:9090"
    echo -e "  ${BLUE}Swagger UI:${NC}  http://localhost:9090/swagger-ui.html"
    echo -e "  ${BLUE}API Docs:${NC}    http://localhost:9090/v3/api-docs"
    echo ""
    echo "Useful commands:"
    echo -e "  ${YELLOW}docker-compose logs -f${NC}              - View all logs"
    echo -e "  ${YELLOW}docker-compose logs -f platform-back${NC} - View backend logs"
    echo -e "  ${YELLOW}docker-compose ps${NC}                   - List running services"
    echo -e "  ${YELLOW}docker-compose stop${NC}                 - Stop all services"
    echo -e "  ${YELLOW}docker-compose down${NC}                 - Stop and remove containers"
    echo ""
    echo "Or use the Makefile:"
    echo -e "  ${YELLOW}make logs${NC}        - View all logs"
    echo -e "  ${YELLOW}make health${NC}      - Check service health"
    echo -e "  ${YELLOW}make stop${NC}        - Stop services"
    echo -e "  ${YELLOW}make help${NC}        - Show all available commands"
    echo ""
}

# Main setup function
main() {
    clear
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║                                                          ║"
    echo "║         TonTin Platform - Setup Script                  ║"
    echo "║                                                          ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo -e "${NC}\n"

    check_prerequisites
    setup_environment

    echo ""
    read -p "Do you want to build and start the services now? (Y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Nn]$ ]]; then
        build_images
        start_services
        show_access_info
    else
        print_info "Setup complete. Run 'docker-compose up' when you're ready to start."
    fi

    echo ""
    print_success "Setup completed successfully!"
}

# Run main function
main
