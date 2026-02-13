.PHONY: help build up down start stop restart logs clean prune dev-backend dev-frontend dev-db backup restore health

# Default target
help:
	@echo "TonTin Platform - Docker Management"
	@echo ""
	@echo "Available commands:"
	@echo "  make build          - Build all Docker images"
	@echo "  make up             - Start all services (build if needed)"
	@echo "  make up-d           - Start all services in detached mode"
	@echo "  make down           - Stop and remove all containers"
	@echo "  make start          - Start existing containers"
	@echo "  make stop           - Stop running containers"
	@echo "  make restart        - Restart all services"
	@echo "  make logs           - View logs from all services"
	@echo "  make logs-back      - View backend logs"
	@echo "  make logs-front     - View frontend logs"
	@echo "  make logs-db        - View database logs"
	@echo "  make ps             - List running containers"
	@echo "  make health         - Check health status of all services"
	@echo "  make clean          - Stop and remove containers, networks"
	@echo "  make clean-all      - Remove containers, networks, volumes, and images"
	@echo "  make prune          - Clean up Docker system"
	@echo "  make dev-backend    - Run only database (for backend dev)"
	@echo "  make dev-frontend   - Run only backend and database (for frontend dev)"
	@echo "  make backup         - Backup database"
	@echo "  make restore        - Restore database from backup"
	@echo "  make shell-back     - Access backend container shell"
	@echo "  make shell-front    - Access frontend container shell"
	@echo "  make shell-db       - Access database container shell"

# Build all images
build:
	docker-compose build

# Build with no cache
build-no-cache:
	docker-compose build --no-cache

# Start all services
up:
	docker-compose up --build

# Start all services in detached mode
up-d:
	docker-compose up -d --build

# Stop and remove containers
down:
	docker-compose down

# Start existing containers
start:
	docker-compose start

# Stop running containers
stop:
	docker-compose stop

# Restart all services
restart:
	docker-compose restart

# Restart specific service
restart-back:
	docker-compose restart platform-back

restart-front:
	docker-compose restart platform-front

restart-db:
	docker-compose restart postgres

# View logs
logs:
	docker-compose logs -f

logs-back:
	docker-compose logs -f platform-back

logs-front:
	docker-compose logs -f platform-front

logs-db:
	docker-compose logs -f postgres

# List containers
ps:
	docker-compose ps

# Check health status
health:
	@echo "Checking service health..."
	@echo "\n=== Backend Health ==="
	@curl -s http://localhost:9090/actuator/health || echo "Backend not responding"
	@echo "\n\n=== Frontend Health ==="
	@curl -s http://localhost/health || echo "Frontend not responding"
	@echo "\n\n=== Database Health ==="
	@docker exec tontin-postgres pg_isready -U happy || echo "Database not responding"
	@echo ""

# Clean up
clean:
	docker-compose down --remove-orphans

# Remove everything including volumes and images
clean-all:
	docker-compose down -v --rmi all --remove-orphans

# Prune Docker system
prune:
	docker system prune -a --volumes

# Development modes
dev-db:
	@echo "Starting only PostgreSQL for local development..."
	docker-compose up postgres

dev-backend:
	@echo "Starting PostgreSQL and Backend for frontend development..."
	docker-compose up postgres platform-back

dev-frontend:
	@echo "Starting all services except frontend for local Angular development..."
	docker-compose up postgres platform-back

# Database operations
backup:
	@echo "Creating database backup..."
	@mkdir -p backups
	@docker exec tontin-postgres pg_dump -U happy tontin_test > backups/backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "Backup created in backups/ directory"

restore:
	@echo "Restoring database from backup..."
	@echo "Available backups:"
	@ls -1 backups/
	@read -p "Enter backup filename: " backup; \
	docker exec -i tontin-postgres psql -U happy tontin_test < backups/$$backup
	@echo "Database restored"

# Access container shells
shell-back:
	docker exec -it tontin-backend sh

shell-front:
	docker exec -it tontin-frontend sh

shell-db:
	docker exec -it tontin-postgres psql -U happy -d tontin_test

# Build individual services
build-back:
	docker-compose build platform-back

build-front:
	docker-compose build platform-front

# Run only specific services
only-db:
	docker-compose up -d postgres

only-back:
	docker-compose up -d postgres platform-back

# Show resource usage
stats:
	docker stats

# Inspect services
inspect-back:
	docker inspect tontin-backend

inspect-front:
	docker inspect tontin-frontend

inspect-db:
	docker inspect tontin-postgres

# Verify installation
verify:
	@echo "Verifying Docker installation..."
	@docker --version
	@docker-compose --version
	@echo "\nDocker is properly installed!"

# Initialize environment
init:
	@echo "Initializing TonTin Platform..."
	@if [ ! -f .env ]; then \
		cp env.example .env; \
		echo "Created .env file. Please update it with your credentials."; \
	else \
		echo ".env file already exists."; \
	fi
	@echo "Run 'make up' to start the application"
