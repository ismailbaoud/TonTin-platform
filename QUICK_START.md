# TonTin Platform - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Prerequisites
- Install [Docker](https://docs.docker.com/get-docker/)
- Install [Docker Compose](https://docs.docker.com/compose/install/)

### Step 2: Setup
```bash
cd TonTin
./setup.sh
```

### Step 3: Access
- **Frontend**: http://localhost
- **Backend API**: http://localhost:9090
- **Swagger UI**: http://localhost:9090/swagger-ui.html

---

## 📋 Essential Commands

### Using Makefile (Recommended)

```bash
make init          # Create .env file
make up-d          # Start all services (background)
make logs          # View all logs
make health        # Check service health
make stop          # Stop services
make down          # Stop and remove containers
make help          # Show all commands
```

### Using Docker Compose

```bash
docker-compose up -d --build        # Start all services
docker-compose logs -f              # View logs
docker-compose ps                   # List services
docker-compose stop                 # Stop services
docker-compose down                 # Stop and remove
```

### Development Mode (Hot Reload)

```bash
# Start with hot reload enabled
docker-compose -f docker-compose.dev.yml up -d

# Access points
# Frontend: http://localhost:4200
# Backend:  http://localhost:9090
# pgAdmin:  http://localhost:5050
```

---

## 🔧 Common Tasks

### View Logs
```bash
make logs              # All services
make logs-back         # Backend only
make logs-front        # Frontend only
make logs-db           # Database only
```

### Restart Services
```bash
make restart           # Restart all
make restart-back      # Restart backend
make restart-front     # Restart frontend
```

### Database Operations
```bash
make backup            # Backup database
make restore           # Restore database
make shell-db          # Access database shell
```

### Troubleshooting
```bash
make health            # Check all services
make ps                # List containers
docker-compose logs -f platform-back   # Debug backend
docker-compose logs -f platform-front  # Debug frontend
```

---

## ⚙️ Configuration

### Email Setup (Required for email features)

1. Edit `.env` file:
```bash
nano .env
```

2. Update email settings:
```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

3. For Gmail, generate App Password:
   - Go to: https://myaccount.google.com/apppasswords
   - Generate password for "Mail"
   - Use this password in `.env`

---

## 🛑 Stop Everything

```bash
make down              # Stop and remove containers
# or
docker-compose down
```

To remove volumes (⚠️ deletes data):
```bash
make clean-all
# or
docker-compose down -v
```

---

## 🆘 Quick Troubleshooting

### Port Already in Use
```bash
# Find what's using the port
sudo netstat -tulpn | grep :80
# Kill the process or change port in docker-compose.yml
```

### Backend Won't Start
```bash
# Check database is running
docker-compose ps postgres
# View backend logs
docker-compose logs platform-back
```

### Can't Connect to API
```bash
# Check backend health
curl http://localhost:9090/actuator/health
# Restart backend
docker-compose restart platform-back
```

### Need Fresh Start
```bash
# Stop everything
make down
# Remove old containers and volumes
make clean-all
# Rebuild and start
make up-d
```

---

## 📚 More Information

- **Detailed Guide**: See [DOCKER_README.md](DOCKER_README.md)
- **Setup Details**: See [DOCKER_SETUP_SUMMARY.md](DOCKER_SETUP_SUMMARY.md)
- **All Commands**: Run `make help`

---

## 🎯 Development Workflow

### Backend Development
```bash
# Option 1: Run backend locally
make dev-db                    # Start only database
cd platform-back
./mvnw spring-boot:run

# Option 2: Use Docker with hot reload
docker-compose -f docker-compose.dev.yml up platform-back
```

### Frontend Development
```bash
# Option 1: Run frontend locally
make dev-frontend              # Start backend + database
cd platform-front
npm install && npm start

# Option 2: Use Docker with hot reload
docker-compose -f docker-compose.dev.yml up platform-front
```

---

## ✅ Health Check

```bash
# Check all services
make health

# Or manually
curl http://localhost:9090/actuator/health    # Backend
curl http://localhost/health                   # Frontend
docker exec tontin-postgres pg_isready -U happy  # Database
```

---

## 🔐 Security Notes

- Never commit `.env` file
- Change default passwords in production
- Use HTTPS in production
- Don't expose database port publicly

---

**Need help?** Check the detailed guides or run `make help` for all available commands.