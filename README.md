# NEU Marketplace

A minimalist, community-driven marketplace built for Northeastern University students to buy and sell items. This is an independent student project and is not affiliated with the university.

## Tech Stack

### Frontend

* **Framework**: React 18 with Vite
* **Language**: TypeScript
* **Styling**: Tailwind CSS & Shadcn/UI
* **State Management**: Zustand (Authentication)
* **Icons & Animation**: Lucide React & Framer Motion

### Backend

* **Framework**: Spring Boot (Java)
* **Security**: Spring Security with JWT and Google OAuth2
* **Storage**: Azure Blob Storage (for listing images)
* **Database**: Spring Data JPA

## Key Features

### Users & Authentication

* **Secure Auth**: Traditional email/password signup with 6-digit verification codes.
* **Social Login**: Google OAuth integration for quick access.
* **Profile Management**: Update personal details, change passwords, and track user statistics.

### Marketplace Listings

* **Discovery**: Search functionality and filters for category, price range, and item condition.
* **Create/Edit**: Support for up to 5 images per listing with markdown descriptions.
* **Lifecycle**: Renew listings ("Bump") to move them to the top of the feed or mark them as "Sold".
* **Saved Items**: Users can heart listings to view them later in their personal library.

### Communication

* **Messaging**: A dedicated inbox for buyers and sellers to coordinate.
* **Real-time Chat**: Polling-based chat interface with unread message indicators.

### Admin & Moderation

* **Dashboard**: Comprehensive overview of user growth, listing statistics, and active reports.
* **User Management**: Admin controls to block users or promote others to admin roles.
* **Content Moderation**: User-driven reporting system for prohibited or fraudulent items.

## Setup

### Prerequisites

* Node.js (v18+)
* Java 17+
* Azure Storage Account (or local emulator)

### Environment Configuration

**Frontend (.env):**

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AUTH_URL=http://localhost:8080

```

**Backend (application.yml):**
Configure your database credentials, JWT secret, and Azure connection string in `src/main/resources/application.yml`.

### Installation

1. **Frontend**:
```bash
npm install
npm run dev

```


2. **Backend**:
```bash
./mvnw spring-boot:run

```



## Disclaimer

This project is an independent student initiative. It is not affiliated with, endorsed by, or connected to Northeastern University.
