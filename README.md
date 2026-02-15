# Pickleballers – Technical Design

## 1. Overview

### 1.1 Purpose

This document describes the technical architecture, system design, data model, and implementation strategy for the Pickleball application. The goal is to provide a scalable, maintainable platform for managing pickleball games, players, rankings, and match history.

### 1.2 Goals

* Provide reliable match recording and score tracking
* Maintain player profiles and ratings
* Support league / group play
* Provide analytics and statistics
* Ensure high availability and data consistency
* Support future mobile and web clients

### 1.3 Non-Goals (Phase 1)

* Real-time video or streaming
* AI coaching or swing analysis
* Complex tournament bracket automation (basic version only)

---

## 2. High-Level Architecture

### 2.1 Architecture Style

* Backend: Modular Monolith (Phase 1)
* Future: Service extraction toward microservices

This allows fast iteration early while preserving clean service boundaries.

### 2.2 System Components

#### Client Layer

* Web frontend (React or similar)
* Future mobile apps (iOS / Android)

#### API Layer

* REST APIs
* Authentication + Authorization
* Request validation

#### Application Layer

* Game Service
* Player Service
* Matchmaking / Rating Service
* League Service
* Notification Service (future)

#### Infrastructure Layer

* PostgreSQL database
* Message routing via Apache Camel (ProducerTemplate)
* Docker container deployment

---

## 3. Technology Stack

### 3.1 Backend

* Java 21+
* Spring Boot 3
* Spring Data JPA
* Spring Security (JWT-based auth)
* Apache Camel (Messaging / Integration)

### 3.2 Database

* PostgreSQL

### 3.3 Infrastructure

* Docker
* Docker Compose (local dev)
* Kubernetes (future)

### 3.4 Observability

* Micrometer
* Prometheus
* Grafana
* Structured logging (JSON logs)

---

## 4. Core Domain Model

### 4.1 Player

Represents a registered user/player.

Fields:

* id (UUID)
* email
* displayName
* rating (ELO or custom rating)
* skillLevel (optional classification)
* createdAt

### 4.2 Match

Represents a completed pickleball match.

Fields:

* id
* matchDate
* location
* matchType (Singles / Doubles)
* winningTeamId
* status (Scheduled, Completed, Cancelled)

### 4.3 Team

Used for doubles or flexible team composition.

Fields:

* id
* matchId
* teamNumber

### 4.4 MatchPlayer

Join table between Player and Match.

Fields:

* id
* matchId
* playerId
* teamId
* score

### 4.5 League (Phase 2)

Fields:

* id
* name
* seasonStart
* seasonEnd

---

## 5. Database Design

### 5.1 Tables

* players
* matches
* teams
* match_players
* leagues (future)

### 5.2 Example Match Query

Fetch full match with players and teams using join fetch or projection DTOs.

---

## 6. API Design

### 6.1 Player APIs

POST /players
GET /players/{id}
GET /players
PATCH /players/{id}

### 6.2 Match APIs

POST /matches
GET /matches/{id}
GET /matches?playerId=
POST /matches/{id}/complete

### 6.3 Auth APIs

POST /auth/register
POST /auth/login
POST /auth/refresh

---

## 7. Rating System

### 7.1 Initial Approach

* ELO-based rating

### 7.2 Update Flow

1. Match completed
2. Rating service calculates delta
3. Player ratings updated in transaction

---

## 8. Messaging Design (Apache Camel)

### 8.1 Usage

* Match completed events
* Rating recalculation
* Notification triggers

### 8.2 Pattern

ProducerTemplate sends domain events:

* match.completed
* player.rating.updated

---

## 9. Transaction Strategy

Critical rule: Match write + rating update must be atomic.

Approach:

* Single DB transaction for match + ratings
* Outbox pattern for event publishing

---

## 10. Security Design

### 10.1 Authentication

* JWT tokens

### 10.2 Authorization

* Player can only modify their own profile
* Admin roles for league management

---

## 11. Deployment

### 11.1 Local Development

Docker Compose:

* app container
* postgres container

### 11.2 Production (Future)

* Kubernetes
* Managed PostgreSQL

---

## 12. Scaling Strategy

### Phase 1

* Single service
* Vertical scaling

### Phase 2

Extract services:

* Rating Service
* Match Service
* Notification Service

---

## 13. Observability

Metrics:

* Match creation rate
* Active players
* API latency

Logging:

* Structured logs
* Correlation IDs

---

## 14. Future Enhancements

* Mobile apps
* League scheduling automation
* Court reservation integrations
* Social features
* Advanced analytics dashboards

---

## 15. Risks

### Data Consistency

Mitigation: Transaction + Outbox pattern

### Rating Accuracy

Mitigation: Simulation testing + historical replay

### Performance

Mitigation: Query optimization + caching

---

## 16. Open Questions

* Do we support guest players?
* How complex should leagues be in v1?
* Do we support real-time score entry?

---

## 17. Implementation Phases

### Phase 1 – MVP

* Player management
* Match recording
* Basic rating

### Phase 2

* Leagues
* Notifications
* Analytics

### Phase 3

* Mobile apps
* Advanced matchmaking

---

## 18. Summary

This design prioritizes:

* Clean domain modeling
* Transaction safety
* Messaging-based extensibility
* Gradual scaling path

The modular monolith approach allows fast delivery while preserving future microservice boundaries.
