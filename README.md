# Eventify - Event Booking Platform

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.x-blue) ![JWT](https://img.shields.io/badge/Security-JWT-purple)

---

## 1. Introduction

Eventify is a robust and scalable backend solution for an event booking platform, built with Java and the Spring Boot framework. It provides a comprehensive set of RESTful APIs for managing users, events, bookings, and payments. The application is designed with a clean, layered architecture and follows modern best practices, including JWT-based security, asynchronous email notifications, and detailed error handling.

---

## 2. Key Features

- **User Authentication**: Secure user registration and login using JWT (JSON Web Tokens).
- **Event Management**: Admins can create, update, and delete events, including complex seating arrangements and pricing.
- **Seat Management**: Dynamic generation of seats with unique numbers and pricing based on rows.
- **Booking System**: Users can book one or more seats for an event. Bookings are initially `PENDING` and are confirmed upon successful payment.
- **Payment Processing**: A mock payment system that allows for payment initiation, confirmation, and failure, with email notifications for each status.
- **Scheduled Tasks**: A background job runs periodically to clean up expired `PENDING` bookings, releasing the seats.
- **Admin Dashboard**: Endpoints to provide statistics for an admin dashboard, including total users, events, bookings, and revenue.
- **Asynchronous Emailing**: Non-blocking email notifications for booking confirmations and payment failures.

---

## 3. Tech Stack

- **Framework**: Spring Boot 3
- **Language**: Java 17
- **Database**: MySQL
- **Security**: Spring Security, JSON Web Tokens (JWT)
- **Data Access**: Spring Data JPA (Hibernate)
- **API Documentation**: (Implicit through this README)
- **Build Tool**: Maven
- **Other Libraries**: Lombok, MapStruct, JJWT

---

## 4. API Endpoints

The base path for all API endpoints is `/api/v1`.

### 4.1. Authentication (`/auth/users`)

| Method | Endpoint             | Description                                      |
|--------|----------------------|--------------------------------------------------|
| `POST` | `/register`          | Registers a new user.                            |
| `POST` | `/login`             | Authenticates a user and returns a JWT token.    |

**Example `POST /register` Request:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "USER"
}
```

### 4.2. Events (`/api/events`)

| Method | Endpoint             | Description                                      |
|--------|----------------------|--------------------------------------------------|
| `POST` | `/`                  | Creates a new event (Admin only).                |
| `GET`  | `/`                  | Retrieves a paginated list of all events.        |
| `GET`  | `/{id}`              | Retrieves a specific event by its ID.            |
| `GET`  | `/venue/{venue}`     | Retrieves a paginated list of events by venue.   |
| `PUT`  | `/{id}`              | Updates an existing event (Admin only).          |
| `DELETE`| `/{id}`             | Deletes an event (Admin only).                   |

**Example `POST /` Request:**
```json
{
  "name": "Live Concert",
  "description": "A live music concert.",
  "venue": "Central Park",
  "eventTimestamp": "2024-10-20T20:00:00",
  "totalSeats": 100,
  "seatsPerRow": 10,
  "seatPricing": [50.0, 40.0, 30.0, 30.0, 25.0, 25.0, 20.0, 20.0, 15.0, 15.0]
}
```

### 4.3. Bookings (`/api/bookings`)

| Method | Endpoint             | Description                                      |
|--------|----------------------|--------------------------------------------------|
| `POST` | `/`                  | Creates a new booking for the authenticated user.|
| `GET`  | `/{id}`              | Retrieves a specific booking by its ID.          |
| `GET`  | `/user`              | Retrieves all bookings for the authenticated user.|
| `PATCH`| `/{id}/cancel`       | Cancels a booking.                               |
| `DELETE`| `/{id}`             | Deletes a booking (Admin only).                  |

**Example `POST /` Request:**
```json
{
  "eventId": 1,
  "seatIds": [1, 2, 3]
}
```

### 4.4. Payments (`/api/payments`)

| Method | Endpoint             | Description                                      |
|--------|----------------------|--------------------------------------------------|
| `POST` | `/initiate/{bookingId}`| Initiates the payment process for a booking.     |
| `POST` | `/webhook`           | Webhook to confirm or fail a payment.            |

**Example `POST /webhook` Request (Success):**
```json
{
  "transactionId": "txn_12345",
  "paymentStatus": "COMPLETED",
  "paymentMethod": "CREDIT_CARD"
}
```

### 4.5. Admin (`/api/admin`)

| Method | Endpoint             | Description                                      |
|--------|----------------------|--------------------------------------------------|
| `GET`  | `/dashboard/stats`   | Retrieves statistics for the admin dashboard.    |
| `GET`  | `/users`             | Retrieves a list of all users.                   |
| `GET`  | `/bookings`          | Retrieves a list of all bookings.                |

---

## 5. Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.x
- MySQL 8.x

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://your-repository-url/eventify.git
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

## 6. Project Structure

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

## 7. Security

- **Authentication**: Handled via a `JWTFilter` that intercepts requests to protected endpoints.
- **Authorization**: Endpoints are secured based on user roles (e.g., admin-only endpoints).
- **Password Encryption**: User passwords are encrypted using `BCryptPasswordEncoder` before being stored in the database.
- **Secret Management**: The JWT secret key is externalized in the `application.properties` file and should be managed securely in a production environment.

---

## 8. Database Schema

- **Users**: Stores user information, including credentials and roles.
- **Events**: Contains details about each event, such as name, venue, date, and seating layout.
- **Seats**: Represents individual seats for an event, including their status (`AVAILABLE`, `LOCKED`, `BOOKED`) and price.
- **Bookings**: Represents a user's booking for an event, linking to the user, event, and the specific seats booked.
- **Payments**: Stores payment information related to a booking, including the transaction ID and status.

---

## 9. Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any bugs or feature requests.
