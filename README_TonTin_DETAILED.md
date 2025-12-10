# 📘 TonTin -- Detailed Project README

## 📝 Overview

**TonTin** is a full-stack web platform that digitizes the traditional
Moroccan rotating savings system known as **"Dâr"**.\
The platform provides organization, tracking, communication, and
rotation management for groups participating in a shared savings cycle
--- without handling real financial transactions.

TonTin aims to bring transparency, structure, and ease of use to a
system widely used across Morocco.

------------------------------------------------------------------------

# 🎯 Objectives

-   Digitalize the management of Moroccan "Dâr" groups.
-   Provide a clear dashboard for organizers and members.
-   Offer a secure authentication system and role access management.
-   Provide communication tools inside each group.
-   Track member participation and monthly rotations effectively.

------------------------------------------------------------------------

# 🚀 Core Features

## 🔐 1. User Management

-   Registration & login (JWT-based)
-   Profile management
-   View all Dârs the user belongs to
-   A user can be:
    -   Organizer in one Dar
    -   Member in another
    -   Member & Organizer across multiple Dârs

------------------------------------------------------------------------

## 🏘️ 2. Dar Management

-   Create and configure a Dâr with:
    -   Amount per cycle
    -   Number of members
    -   Start date
    -   Tour generation mode (automatic / manual)
-   Invite members via code or link
-   Manage join requests
-   Activate / pause the Dâr

------------------------------------------------------------------------

## 👥 3. Member Management

-   Users can request to join a Dâr
-   Organizers can approve or reject requests
-   Remove / ban members (based on rules)
-   Track each member's status inside each Dâr

------------------------------------------------------------------------

## 🔄 4. Tour (Rotation) Management

-   Automatic tour generation for all members
-   Manual editing for special cases
-   Guaranteed non-repetition
-   Store previous and future tour assignments
-   Clear view of monthly beneficiary

------------------------------------------------------------------------

## 💬 5. Communication

-   Internal chat per Dâr
-   Announcements from organizers
-   Notifications for:
    -   Join request accepted
    -   New cycle started
    -   Beneficiary announcement

------------------------------------------------------------------------

## 📊 6. Dashboard

-   Dar overview
-   Member list
-   Tour calendar
-   Activity timeline
-   Progress tracking

------------------------------------------------------------------------

# 🏛️ System Architecture

## 🖥 Backend -- Spring Boot

-   Spring Boot 3
-   Spring Security (JWT)
-   JPA / Hibernate
-   PostgreSQL
-   DTO / Services / Controllers layered architecture
-   Centralized exception handling
-   Role-based authorization

------------------------------------------------------------------------

## 💻 Frontend -- Angular

-   Angular 17
-   Angular Material / PrimeNG UI components
-   RxJS state management
-   Modular architecture
-   Reusable shared components
-   Responsive design

------------------------------------------------------------------------

# 🧱 Database Structure

## Main Tables:

-   **Users**
-   **Dars**
-   **DarMembers** (relation table with roles per Dar)
-   **Tours**
-   **Messages**
-   **Notifications**

### Example of role handling:

A user's permission is determined **per Dâr**, not globally.

------------------------------------------------------------------------

# 📂 Project Structure

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

------------------------------------------------------------------------

# ⚙️ Installation & Setup

## Backend Setup

``` bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Default backend port:** 8080

------------------------------------------------------------------------

## Frontend Setup

``` bash
cd frontend
npm install
ng serve
```

**Default frontend port:** 4200

------------------------------------------------------------------------

# 🔍 Testing

## Backend Tests

-   JUnit 5\
-   Mockito\
    Run tests:

``` bash
mvn test
```

## Frontend Tests

-   Jasmine\
-   Karma\
    Run tests:

``` bash
ng test
```

------------------------------------------------------------------------

# 🚀 Deployment Options

-   Docker containers\
-   Railway / Render / Heroku\
-   VPS + Nginx reverse proxy\
-   Spring Boot JAR deployment

------------------------------------------------------------------------

# 📘 API Documentation

Swagger UI available at:

    /swagger-ui.html

Exposes all endpoints with models and responses.

------------------------------------------------------------------------

# 🛡️ Security

-   JWT Authentication
-   Role-based access control (Admin, Organizer, Member)
-   Input validation
-   OWASP protection standards

------------------------------------------------------------------------

# 🤝 Contribution Guidelines

1.  Fork the repository\
2.  Create a feature branch\
3.  Commit your updates\
4.  Open a pull request

------------------------------------------------------------------------

# 🖊️ Author

**TonTin Project**\
Full-stack Web Application\
Spring Boot + Angular

------------------------------------------------------------------------

# 📄 License

This project is licensed under the **MIT License**.
