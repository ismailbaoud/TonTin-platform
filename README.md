# 📘 TonTin - Rotation Management Platform

[TontTin specifications](https://1drv.ms/w/c/f4fee283c02594e2/IQAcW0ZWdf7_QLAgHjDEWnOxAVboQS-zoPOA4mpw47VxErU?e=xCDVtN)

## 📝 Overview

TonTin is a full-stack web platform designed to manage **rotation-based group systems** inspired by the Moroccan concept of *Dâr*, without focusing on savings or money handling.

The platform helps groups organize members, manage turns, coordinate cycles, and communicate transparently — **no financial transactions are processed or tracked**.

TonTin focuses on organization, fairness, and visibility, making it suitable for community groups, associations, cooperatives, or any structured rotation-based collaboration.

---

## 🎯 Objectives

- Digitize the management of rotation-based groups
- Provide transparent rotation and cycle tracking
- Enable role-based group coordination
- Improve internal communication
- Ensure fair and non-repetitive turn assignment

---

## 🚀 Core Features

### 🔐 1. User Management

- User registration & login (JWT-based)
- Profile management
- View all groups the user participates in
- A user can be:
  - Organizer in one group
  - Member in another
  - Organizer & Member across multiple groups

---

### 🏘️ 2. Group Management

- Create and configure a group with:
  - Number of participants
  - Cycle duration
  - Start date
  - Rotation generation mode (automatic / manual)
- Invite members via code or link
- Manage join requests
- Activate or pause a group

---

### 👥 3. Member Management

- Request to join a group
- Organizer approval or rejection
- Remove or suspend members
- Track member status per group

---

### 🔄 4. Rotation Management

- Automatic rotation generation
- Manual editing for exceptions
- Guaranteed non-repetition
- Track previous, current, and future turns
- Clear visibility of assigned participants per cycle

---

### 💬 5. Communication

- Group chat
- Organizer announcements
- Notifications for:
  - Join request updates
  - Cycle start
  - Turn assignments
  - Group state changes

---

### 📊 6. Dashboard

- Group overview
- Member list
- Rotation calendar
- Activity timeline
- Cycle progress tracking

---

## 🏛️ System Architecture

### 🖥 Backend — Spring Boot

- Spring Boot 3
- Spring Security (JWT)
- JPA / Hibernate
- PostgreSQL
- Layered architecture (Controller / Service / DTO / Repository)
- Centralized exception handling
- Group-based role authorization

---

### 💻 Frontend — Angular

- Angular 17
- Angular Material / PrimeNG
- RxJS state management
- Modular architecture
- Reusable shared components
- Responsive UI

---

## 🧱 Database Structure

### Main Tables

- Users
- Groups
- GroupMembers
- Rotations
- Messages
- Notifications

User permissions are defined **per group**, not globally.

---

## 📂 Project Structure

TonTin/
 ├── backend/
 │    ├── src/
 │    ├── pom.xml
 │    └── application.properties
 │
 ├── frontend/
 │    ├── src/
 │    ├── angular.json
 │    └── package.json
 │
 └── README.md

---

## ⚙️ Installation & Setup

### Backend

cd backend  
mvn clean install  
mvn spring-boot:run  

Default port: 8080

---

### Frontend

cd frontend  
npm install  
ng serve  

Default port: 4200

---

## 🔍 Testing

### Backend

- JUnit 5
- Mockito

mvn test

---

### Frontend

- Jasmine
- Karma

ng test

---

## 🚀 Deployment

- Docker
- Railway / Render / Heroku
- VPS with Nginx
- Spring Boot JAR deployment

### Docker quick start

1. Copy Docker env template:

```bash
cp .env.docker.example .env
```

2. Start production-like stack (Postgres + Backend + Frontend):

```bash
docker compose up --build
```

3. Access services:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:9090/api`

4. Start development stack with hot reload:

```bash
docker compose -f docker-compose.dev.yml up --build
```

---

## 📘 API Documentation

Swagger UI:

/swagger-ui.html

---

## 🛡️ Security

- JWT authentication
- Group-based role access
- Input validation
- OWASP best practices
- No financial processing

---

## 🤝 Contribution

- Fork the repository
- Create a feature branch
- Commit changes
- Open a pull request

---

## 🖊️ Author

TonTin Project — v2  
Rotation Management Platform  
Spring Boot + Angular

---
## 📄 License

MIT License
