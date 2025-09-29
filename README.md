# Eventify - Event Booking Platform

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.x-blue) ![JWT](https://img.shields.io/badge/Security-JWT-purple)

---

## 1. Introduction

Eventify is a robust and scalable backend solution for an event booking platform, built with Java and the Spring Boot framework. It provides a comprehensive set of RESTful APIs for managing users, events, bookings, and payments. The application is designed with a clean, layered architecture and follows modern best practices, including JWT-based security, asynchronous email notifications, and detailed error handling.

---

## 2. Key Features

- **User Authentication**: Secure user registration and login using JWT (JSON Web Tokens).
- **Event Management**: Admins and vendors can create, update, and delete events.
- **Seat Management**: Dynamic generation of seats with unique numbers and pricing based on rows.
- **Booking System**: Users can book seats for an event, with bookings confirmed upon successful payment.
- **Payment Processing**: A mock payment system that handles payment initiation, confirmation, and failure.
- **Scheduled Tasks**: A background job runs periodically to clean up expired pending bookings, releasing the seats.
- **Admin Dashboard**: Endpoints to provide statistics for an admin dashboard, including total users, events, bookings, and revenue.
- **Asynchronous Emailing**: Non-blocking email notifications for booking confirmations and payment failures.

---

## 3. Tech Stack

- **Framework**: Spring Boot 3
- **Language**: Java 17
- **Database**: MySQL
- **Security**: Spring Security, JSON Web Tokens (JWT)
- **Data Access**: Spring Data JPA (Hibernate)
- **API Documentation**: SpringDoc (Swagger UI)
- **Build Tool**: Maven
- **Other Libraries**: Lombok, MapStruct, JJWT

---

## 4. API Overview

The API is organized around REST principles, with the base path for all endpoints being `/api/v1`.

- **Authentication (`/auth/users`):** Handles user registration and login.
- **Events (`/api/events`):** Provides operations for creating, retrieving, updating, and deleting events.
- **Bookings (`/api/bookings`):** Allows users to create, view, and manage their event bookings.
- **Payments (`/api/payments`):** Manages the payment process for bookings.
- **Admin (`/api/admin`):** Exposes administrative endpoints for monitoring and managing the platform.

For a detailed and interactive exploration of all endpoints, please refer to the Swagger UI documentation.

---

## 5. API Documentation (Swagger UI)

Once the application is running, you can access the interactive API documentation via Swagger UI at the following URL:

[http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)

The Swagger UI allows you to explore all the API endpoints, view their details, and test them directly from your browser.

---

## 6. Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.x
- MySQL 8.x

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/myselfavaneesh/eventify-ticket-booking.git
    cd eventify
    ```

2.  **Configure the database:**
    - Create a new MySQL database named `eventifyDb`.
    - Open `src/main/resources/application.properties` and update the `spring.datasource` properties with your MySQL username and password.

3.  **Configure email settings:**
    - In `application.properties`, update the `spring.mail` properties with your email provider's details. For Gmail, you will need to generate an "App Password".

4.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

---

## 7. Project Structure

```
/src/main/java/com/avaneesh/yodha/Eventify
├── configs       # Spring Boot configurations (e.g., AsyncConfig)
├── controllers   # REST API controllers (request/response handling)
├── dto           # Data Transfer Objects (for API requests and responses)
├── entities      # JPA entity classes (database table mappings)
├── enums         # Enumerations (e.g., BookingStatus, SeatStatus)
├── exception     # Custom exception classes and global exception handler
├── mapper        # MapStruct mappers for converting between entities and DTOs
├── repository    # Spring Data JPA repositories (database queries)
├── security      # Security configurations (JWT filter, UserDetailsService)
├── services      # Business logic layer
└── utils         # Utility classes (e.g., JWTUtility, ApiResponse)
```

---

## 8. Security

- **Authentication**: Handled via a `JWTFilter` that intercepts requests to protected endpoints.
- **Authorization**: Endpoints are secured based on user roles (e.g., admin-only endpoints).
- **Password Encryption**: User passwords are encrypted using `BCryptPasswordEncoder` before being stored in the database.
- **Secret Management**: The JWT secret key is externalized in the `application.properties` file and should be managed securely in a production environment.

---

## 9. Database Schema

- **Users**: Stores user information, including credentials and roles.
- **Events**: Contains details about each event, such as name, venue, date, and seating layout.
- **Seats**: Represents individual seats for an event, including their status (`AVAILABLE`, `LOCKED`, `BOOKED`) and price.
- **Bookings**: Represents a user's booking for an event, linking to the user, event, and the specific seats booked.
- **Payments**: Stores payment information related to a booking, including the transaction ID and status.

---

## 10. Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any bugs or feature requests.
