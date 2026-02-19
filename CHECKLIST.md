# TonTin Platform - Setup & Deployment Checklist

## 📋 Pre-Installation Checklist

### System Requirements
- [ ] Operating System: Linux, macOS, or Windows with WSL2
- [ ] Docker installed (version 20.10+)
- [ ] Docker Compose installed (version 2.0+)
- [ ] Minimum 4GB RAM available
- [ ] Minimum 10GB free disk space
- [ ] Internet connection for downloading images

### Verify Installation
```bash
docker --version          # Should show 20.10+
docker-compose --version  # Should show 2.0+
docker info              # Should run without errors
```

---

## 🚀 Initial Setup Checklist

### 1. Clone & Navigate
- [ ] Repository cloned successfully
- [ ] Navigated to TonTin directory
- [ ] All files present (check with `ls -la`)

### 2. Environment Configuration
- [ ] Copy `env.example` to `.env`
- [ ] Review database credentials in `.env`
- [ ] Update email settings (if using email features)
  - [ ] Gmail address set as `MAIL_USERNAME`
  - [ ] App Password generated and set as `MAIL_PASSWORD`
  - [ ] App Password generated from: https://myaccount.google.com/apppasswords
- [ ] Review all environment variables
- [ ] Ensure `.env` is in `.gitignore` (security)

### 3. Docker Files Verification
- [ ] `docker-compose.yml` exists in root
- [ ] `docker-compose.dev.yml` exists in root
- [ ] `platform-back/Dockerfile` exists
- [ ] `platform-back/Dockerfile.dev` exists
- [ ] `platform-front/Dockerfile` exists
- [ ] `platform-front/Dockerfile.dev` exists
- [ ] `platform-front/nginx.conf` exists
- [ ] Both `.dockerignore` files exist

---

## 🏗️ Build & Start Checklist

### Option A: Automated Setup (Recommended)
- [ ] Run `./setup.sh`
- [ ] Script completes without errors
- [ ] All prerequisites passed
- [ ] Environment file created
- [ ] Images built successfully
- [ ] Services started successfully

### Option B: Manual Setup
- [ ] Run `docker-compose build`
- [ ] Build completes without errors (may take 5-10 minutes)
- [ ] Run `docker-compose up -d`
- [ ] All three services start successfully
- [ ] No error messages in output

### Verify Services Started
```bash
docker-compose ps
```
- [ ] `tontin-postgres` - Status: Up (healthy)
- [ ] `tontin-backend` - Status: Up (healthy)
- [ ] `tontin-frontend` - Status: Up (healthy)

---

## ✅ Health Check Checklist

### 1. Container Health
```bash
make health
# or
docker-compose ps
```
- [ ] PostgreSQL shows as "healthy"
- [ ] Backend shows as "healthy"
- [ ] Frontend shows as "healthy"

### 2. Network Connectivity
- [ ] Backend can reach database
- [ ] Frontend can reach backend
- [ ] All containers on same network

### 3. Service Endpoints

#### Backend Health
```bash
curl http://localhost:9090/actuator/health
```
- [ ] Returns HTTP 200
- [ ] Response contains `"status":"UP"`

#### Frontend Health
```bash
curl http://localhost/health
```
- [ ] Returns HTTP 200
- [ ] Response contains "healthy"

#### Database Health
```bash
docker exec tontin-postgres pg_isready -U happy
```
- [ ] Returns "accepting connections"

---

## 🌐 Access Verification Checklist

### Frontend (Angular)
- [ ] Open http://localhost in browser
- [ ] Page loads without errors
- [ ] No console errors in browser DevTools
- [ ] Styling loads correctly
- [ ] Navigation works

### Backend API
- [ ] Open http://localhost:9090 in browser
- [ ] Server responds (may show Whitelabel Error Page - this is OK)
- [ ] API accessible at http://localhost:9090/api/*

### Swagger UI
- [ ] Open http://localhost:9090/swagger-ui.html
- [ ] Swagger UI loads correctly
- [ ] All API endpoints listed
- [ ] Can expand and view endpoint details
- [ ] Try executing a GET endpoint

### Database
- [ ] Database accessible via psql:
```bash
docker exec -it tontin-postgres psql -U happy -d tontin_test
```
- [ ] Can list tables with `\dt`
- [ ] Can execute queries
- [ ] Exit with `\q`

---

## 🔍 Logs Verification Checklist

### View All Logs
```bash
docker-compose logs
```
- [ ] No critical errors
- [ ] No connection failures
- [ ] No startup exceptions

### Backend Logs
```bash
docker-compose logs platform-back
```
- [ ] Spring Boot started successfully
- [ ] Database connection established
- [ ] No stack traces or errors
- [ ] Application running on port 9090
- [ ] Hibernate initialized

### Frontend Logs
```bash
docker-compose logs platform-front
```
- [ ] Nginx started successfully
- [ ] No error messages
- [ ] Serving files correctly

### Database Logs
```bash
docker-compose logs postgres
```
- [ ] PostgreSQL started successfully
- [ ] Database initialized
- [ ] Ready to accept connections
- [ ] No permission errors

---

## 🧪 Functional Testing Checklist

### Basic Functionality
- [ ] Can access frontend homepage
- [ ] Can view API documentation (Swagger)
- [ ] Can make API calls from Swagger UI
- [ ] Backend responds to API requests
- [ ] Database persists data

### Data Persistence Test
1. [ ] Create data through API or application
2. [ ] Stop containers: `docker-compose stop`
3. [ ] Start containers: `docker-compose start`
4. [ ] Verify data still exists
5. [ ] Data persistence confirmed

### Email Functionality (If Configured)
- [ ] Email credentials set in `.env`
- [ ] Backend can connect to SMTP server
- [ ] Test email sends successfully
- [ ] No authentication errors in logs

---

## 🛠️ Development Mode Checklist (Optional)

### Start Development Environment
```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Verify Development Features
- [ ] Angular dev server accessible at http://localhost:4200
- [ ] Backend accessible at http://localhost:9090
- [ ] pgAdmin accessible at http://localhost:5050
- [ ] Debug port 5005 available for backend
- [ ] Hot reload works for frontend
- [ ] Hot reload works for backend

### pgAdmin Setup
- [ ] Login to pgAdmin (admin@tontin.com / admin)
- [ ] Add server connection:
  - Host: postgres
  - Port: 5432
  - Username: happy
  - Password: happy
  - Database: tontin_test
- [ ] Can view database schema
- [ ] Can execute queries

---

## 📦 Backup & Restore Checklist

### Backup
```bash
make backup
# or
docker exec tontin-postgres pg_dump -U happy tontin_test > backup.sql
```
- [ ] Backup file created
- [ ] Backup file not empty
- [ ] Backup file contains SQL statements

### Restore
```bash
make restore
# or
docker exec -i tontin-postgres psql -U happy tontin_test < backup.sql
```
- [ ] Restore completes without errors
- [ ] Data restored successfully
- [ ] Can query restored data

---

## 🔒 Security Checklist

### Configuration Security
- [ ] `.env` file NOT committed to git
- [ ] Default passwords changed (production)
- [ ] Strong database password set (production)
- [ ] Email credentials secured
- [ ] No sensitive data in docker-compose.yml

### Container Security
- [ ] Backend running as non-root user
- [ ] Minimal base images used (Alpine)
- [ ] No unnecessary ports exposed
- [ ] Health checks enabled
- [ ] Resource limits set (production)

### Network Security
- [ ] Database not directly exposed (production)
- [ ] Only necessary ports open
- [ ] Containers on isolated network
- [ ] HTTPS configured (production)

---

## 🚢 Production Deployment Checklist

### Pre-Deployment
- [ ] All development features disabled
- [ ] Environment variables updated for production
- [ ] Strong passwords set for all services
- [ ] Email configuration verified
- [ ] SSL certificates obtained
- [ ] Domain names configured
- [ ] Firewall rules configured

### Deployment
- [ ] Use `docker-compose.yml` (not dev version)
- [ ] Resource limits configured
- [ ] Logging configured
- [ ] Monitoring set up
- [ ] Backup strategy implemented
- [ ] Restart policy set to "always"

### Post-Deployment
- [ ] All services healthy
- [ ] HTTPS working correctly
- [ ] Domain resolves correctly
- [ ] API accessible
- [ ] Database backed up
- [ ] Monitoring alerts configured
- [ ] Documentation updated

### Production Testing
- [ ] Load testing performed
- [ ] Security scan completed
- [ ] Backup/restore tested
- [ ] Failover tested
- [ ] Performance acceptable
- [ ] No memory leaks
- [ ] No connection leaks

---

## 🧹 Maintenance Checklist

### Regular Maintenance (Weekly)
- [ ] Check container health: `make health`
- [ ] Review logs for errors: `make logs`
- [ ] Monitor disk usage: `docker system df`
- [ ] Check resource usage: `docker stats`
- [ ] Verify backups working

### Monthly Maintenance
- [ ] Update Docker images
- [ ] Review security updates
- [ ] Test backup restoration
- [ ] Clean up unused resources: `docker system prune`
- [ ] Review and rotate logs
- [ ] Check database size and optimize

### When Issues Occur
- [ ] Check service health: `make health`
- [ ] Review recent logs: `make logs`
- [ ] Check disk space: `df -h`
- [ ] Check container status: `docker-compose ps`
- [ ] Restart affected service: `make restart-[service]`
- [ ] Full restart if needed: `make restart`

---

## 🆘 Troubleshooting Checklist

### Services Won't Start
- [ ] Check Docker daemon running: `docker info`
- [ ] Check port availability: `netstat -tulpn | grep -E '80|9090|5432'`
- [ ] Review logs: `docker-compose logs`
- [ ] Check `.env` file exists and is valid
- [ ] Verify disk space available: `df -h`
- [ ] Try clean rebuild: `make clean-all && make up-d`

### Database Connection Issues
- [ ] Verify PostgreSQL is running: `docker-compose ps postgres`
- [ ] Check database health: `docker exec tontin-postgres pg_isready -U happy`
- [ ] Review database logs: `docker-compose logs postgres`
- [ ] Verify credentials in `.env` match application.properties
- [ ] Check network connectivity: `docker network ls`

### Frontend Not Loading
- [ ] Check Nginx is running: `docker-compose ps platform-front`
- [ ] Review frontend logs: `docker-compose logs platform-front`
- [ ] Test Nginx config: `docker exec tontin-frontend nginx -t`
- [ ] Verify build output exists: `docker exec tontin-frontend ls /usr/share/nginx/html`
- [ ] Check browser console for errors

### Backend API Errors
- [ ] Check Spring Boot logs: `docker-compose logs platform-back`
- [ ] Verify Java version: `docker exec tontin-backend java -version`
- [ ] Check database connection
- [ ] Verify WAR file exists: `docker exec tontin-backend ls -la /app`
- [ ] Test health endpoint: `curl http://localhost:9090/actuator/health`

---

## ✨ Success Criteria

All the following should be true for a successful deployment:

- [ ] ✅ All containers are running and healthy
- [ ] ✅ Frontend accessible at http://localhost
- [ ] ✅ Backend API responding at http://localhost:9090
- [ ] ✅ Swagger UI accessible and working
- [ ] ✅ Database accepting connections
- [ ] ✅ No critical errors in logs
- [ ] ✅ Data persists after restart
- [ ] ✅ All health checks passing
- [ ] ✅ Resources within acceptable limits
- [ ] ✅ Backup and restore working

---

## 📚 Additional Resources

- Detailed Guide: [DOCKER_README.md](DOCKER_README.md)
- Quick Start: [QUICK_START.md](QUICK_START.md)
- Architecture: [DOCKER_ARCHITECTURE.md](DOCKER_ARCHITECTURE.md)
- Setup Summary: [DOCKER_SETUP_SUMMARY.md](DOCKER_SETUP_SUMMARY.md)
- Available Commands: Run `make help`

---

**Document Version**: 1.0.0  
**Last Updated**: February 2025
