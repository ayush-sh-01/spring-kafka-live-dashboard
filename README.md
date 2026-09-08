# Real-Time Analytics Dashboard & Reporting Engine

A high-performance real-time analytics and event streaming reporting engine built with **Spring Boot 3**, **Apache Kafka**, **Hibernate 6 (L2 + Query Cache via Ehcache 3)**, **PostgreSQL / H2**, and **WebSockets (STOMP)**.

---

## 🚀 Overview

This application demonstrates an enterprise-grade analytics pipeline designed to ingest, process, cache, and visualize high-throughput user activity events in real time.

```
[Client / Traffic Simulator]
            │
            ▼ (REST / Kafka Producer)
   [Apache Kafka Topic]
            │
            ▼ (Kafka Consumer / Ingestion)
   [Spring Boot Service] ──► [Hibernate L2 & Query Cache (Ehcache)]
            │                         │
            ▼                         ▼
   [PostgreSQL / H2 DB]      [WebSocket (STOMP Broker)]
                                      │
                                      ▼
                           [Live Web Dashboard]
```

---

## ✨ Features

- **High-Throughput Event Ingestion**: Asynchronous event publishing and consumption using Apache Kafka.
- **Multi-Level Caching**: Hibernate Second-Level (L2) entity caching and Query Caching powered by Ehcache 3 (JSR-107) to minimize database load.
- **Real-Time Live Updates**: Push notifications via WebSocket (STOMP) directly to client dashboards on incoming events.
- **Dynamic Multi-Criteria Search**: Dynamic querying using JPA Criteria API and `@NamedEntityGraph` to eliminate N+1 queries.
- **Interactive UI Dashboard**: Embedded dark-mode live analytics dashboard with real-time charts and traffic simulation.
- **Dev & Prod Profiles**:
  - `dev`: Runs with in-memory H2 database and in-process simulation (no external Kafka/Postgres required).
  - `prod`: Runs with full Docker-based PostgreSQL and Apache Kafka broker.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA, Spring Kafka, Spring WebSocket
- **Database**: PostgreSQL 15 / H2 In-Memory
- **Caching**: Hibernate 6 + Ehcache 3 (Jakarta JCache provider)
- **Messaging**: Apache Kafka + Zookeeper, STOMP over SockJS
- **Frontend**: Responsive HTML5, Vanilla JavaScript, CSS, Chart.js

---

## 🚦 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- *(Optional for Prod)* **Docker & Docker Compose**

---

### 1. Run in Development Mode (Quick Start)

The `dev` profile uses an in-memory H2 database and standalone event processing without needing Docker:

```bash
# Clone the repository
git clone <repo-url>
cd RealTime_Analytics_Dashboard_project_code

# Build and run with Spring Boot (Dev profile is active by default)
./mvnw spring-boot:run
```

*(On Windows PowerShell / CMD)*:
```powershell
mvn clean spring-boot:run
```

Access the dashboard in your browser:
👉 **[http://localhost:8080](http://localhost:8080)**  
👉 **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:analytics_db`, User: `sa`, Password: *(empty)*)

---

### 2. Run with Docker (Production Mode: Kafka + PostgreSQL)

To run with full Kafka event streaming and PostgreSQL:

1. **Start Kafka and PostgreSQL containers**:
   ```bash
   docker-compose up -d
   ```

2. **Run the application with the `prod` profile**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 📡 API Endpoints

### Event Ingestion & Simulation

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/events` | Ingest a single activity event into the pipeline |
| `POST` | `/api/events/simulate?count=25` | Generate and stream simulated clickstream events |

#### Sample Event Payload (`POST /api/events`):
```json
{
  "username": "alice_wonder",
  "country": "United States",
  "city": "New York",
  "ipAddress": "198.51.100.10",
  "browser": "Chrome",
  "os": "Windows 11",
  "deviceType": "Desktop",
  "action": "PURCHASE",
  "endpoint": "/api/checkout",
  "responseTimeMs": 45
}
```

---

### Reports & Analytics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reports/summary` | Fetch high-level summary metrics (Cached) |
| `GET` | `/api/reports/recent` | Fetch top 50 recent detailed activity logs |
| `POST` | `/api/reports/filter` | Paginated dynamic multi-criteria filter search |

---

### WebSockets

- **Handshake URL**: `ws://localhost:8080/ws-analytics` (with SockJS fallback)
- **Subscribe Topic**: `/topic/analytics-summary` (Receives real-time analytics updates)

---

## 📁 Project Structure

```
.
├── docker-compose.yml              # PostgreSQL, Kafka & Zookeeper setup
├── pom.xml                         # Project dependencies and plugins
├── src/main/java/com/analytics/engine
│   ├── AnalyticsReportingEngineApplication.java
│   ├── config/                     # WebSocket & Kafka configuration
│   ├── controller/                 # REST Controllers (Ingest, Reports)
│   ├── dto/                        # Request and Response DTOs
│   ├── model/                      # JPA Entities (ActivityLog, User, etc.)
│   ├── repository/                 # Spring Data JPA Repositories
│   └── service/                    # Business Logic, Kafka Producer/Consumer, Caching
└── src/main/resources
    ├── application.yml             # Spring Boot configuration (dev & prod)
    ├── ehcache.xml                 # L2 and Query Cache configuration
    └── static/                     # Real-time web dashboard (index.html)
```

---

## 📄 License

This project is licensed under the MIT License.
