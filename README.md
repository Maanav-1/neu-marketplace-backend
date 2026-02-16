# NEU Marketplace - Backend

The core API for NEU Marketplace, providing a secure and scalable backend for the student community platform. Built with a focus on modularity, security, and efficient media handling.

## Core Features

### Authentication & Security

* **JWT-Based Auth**: Secure stateless authentication using JSON Web Tokens.
* **OAuth2 Integration**: Support for Google Sign-In alongside traditional email/password accounts.
* **Email Verification**: Automated 6-digit verification codes for account security and password resets.
* **Role-Based Access**: Granular control for standard users and administrators.

### Marketplace & Media

* **Listing Management**: Complete CRUD operations for item listings, including categories, conditions, and pricing.
* **Image Processing**: Integration with **Azure Blob Storage** for high-availability image hosting and management.
* **Search & Filters**: Backend support for complex querying based on category, price range, and item status.

### Communication & Interactions

* **Messaging System**: Manages real-time conversations and message history between buyers and sellers.
* **Saved Items**: Logic for user interest tracking and "hearting" listings.
* **Reporting System**: Infrastructure for users to flag content, which is then queued for admin review.

### Administrative Tools

* **Statistics Engine**: Aggregates data for user growth, listing trends, and category performance.
* **Moderation API**: Endpoints for blocking/unblocking users and resolving content reports.
* **Cleanup Service**: Automated tasks for managing expired listings and temporary data.

## Tech Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3
* **Security**: Spring Security + JWT + OAuth2
* **Persistence**: Spring Data JPA (Hibernate)
* **Storage**: Azure Blob Storage
* **Build Tool**: Maven
* **Database**: MySQL/PostgreSQL compatible

## Project Structure

* `controller/`: REST API endpoints and request mapping.
* `service/`: Core business logic and external integrations (Email, Azure, etc).
* `model/`: JPA entities and database schema definitions.
* `repository/`: Data access layer.
* `security/`: JWT filters, OAuth2 handlers, and security configuration.
* `dto/`: Request and response objects for data transfer.

## Setup

1. **Prerequisites**:
* JDK 17
* Maven
* A relational database (MySQL/PostgreSQL)
* Azure Storage account (for image features)


2. **Configuration**:
Update `src/main/resources/application.yml` with your credentials:
* Database URL/Username/Password
* JWT Secret
* OAuth2 Client IDs
* Azure Connection String


3. **Run**:
```bash
./mvnw spring-boot:run

```



## Disclaimer

This is an independent student project developed at Northeastern University and is not officially affiliated with the institution.
