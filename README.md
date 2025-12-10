# TonTin -- Platform README

## Overview

TonTin is a web-based platform designed to manage the traditional
Moroccan rotating savings system known as "Dâr". The platform digitizes
organization, tracking, and communication within savings groups without
handling real financial transactions.

## Features

-   User registration & authentication (JWT)
-   Create and configure Dârs
-   Manage members and invitations
-   Automatic or manual tour (rotation) generation
-   Internal messaging per Dar
-   Dashboard with group statistics
-   Role-based access (Admin, Organizer, Member)

## Technologies

### Backend

-   Spring Boot 3\
-   Spring Security\
-   PostgreSQL\
-   REST API

### Frontend

-   Angular 17\
-   Angular Material / PrimeNG

## Installation

### Backend

    cd backend
    mvn clean install
    mvn spring-boot:run

### Frontend

    cd frontend
    npm install
    ng serve

## Project Structure

    /backend
    /frontend

## License

MIT License

## Author

TonTin Team
