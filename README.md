# AnnaDhata (अन्नदाता)
### *Smart Food Waste Reduction and Sustainable Redistribution Ecosystem*
> **Tagline:** *"Turning Surplus Food Into Meals, Not Waste."*

[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-blue.svg)](https://www.typescriptlang.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0+-green.svg)](https://www.mongodb.com/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.4-38B2AC.svg)](https://tailwindcss.com/)
[![Gemini AI](https://img.shields.io/badge/Google%20Gemini-1.5%20Flash-orange.svg)](https://deepmind.google/technologies/gemini/)

---

## Executive Summary

**AnnaDhata** is a production-grade, full-stack, AI-driven food rescue and redistribution ecosystem connecting commercial food donors with verified NGOs, shelters, and volunteer couriers before pickup deadlines elapse.

---

## Key Features & Platform Architecture

### 1. Core Surplus & Claim Redistribution
- **Partial Food Distribution:** Multiple verified NGOs can claim customizable partial quantities from a single large surplus listing (`PARTIALLY_CLAIMED`) using atomic MongoDB `findAndModify` reservations that enforce strict `remainingQuantity >= claimedQuantity` guarantees and prevent overselling.
- **QR Food Passport (ZXing + HMAC-SHA256):** Every approved claim automatically generates a signed cryptographic token (`AD-...`) and 260x260 Base64 PNG QR image (`PassportService`) to authenticate physical pickup handovers (`ACTIVE` -> `VERIFIED_PICKUP`).
- **Rescue Relay:** Scheduled background scanner (`RescueRelayService`, every 60 seconds with a 15-minute grace window) detects overdue approved pickups, alerts donors/receivers, lets donors approve releasing delayed allocations back to `remainingQuantity`, revokes stale passports, and automatically re-matches alternative nearby NGOs while excluding late receivers.
- **AI Demand Heatmap:** Clusters real historical MongoDB donations, expired food waste hotspots, completed rescues, and active NGO demand (`HeatmapService`) across configurable time windows and food categories, visualized via interactive Leaflet zones and Gemini spatial strategy insights.
- **Volunteer Network:** Dedicated `ROLE_VOLUNTEER` accounts with customizable vehicle capacity, service radius, and availability (`VolunteerProfile`), mission discovery board, atomic `findAndModify` mission acceptance, QR pickup verification, and NGO delivery completion.
- **Trust & Reliability Score:** Transparent 0-100 multi-factor reliability scoring (`PLATINUM`, `GOLD`, `SILVER`, `NEEDS_IMPROVEMENT`) calculated from real MongoDB history across Donors, Receivers, and Volunteers, surfaced cleanly with inspectable factor breakdowns (`TrustBadge`) and factored into NGO matching priority.

### 2. Google Gemini AI Integration (with Deterministic Guardrails & Fallback)
- **Natural-Language Donation Assistant:** Extracts structured JSON (food name, category, quantity, unit, preparation/pickup clock times, handling instructions, storage conditions) while requiring explicit human confirmation prior to publication.
- **Smart 4-Factor + Trust Matching Engine:** Deterministic weighted ranking (`Distance 30%`, `Quantity & Capacity 25%`, `Urgency 25%`, `Food Compatibility 20%`, plus transparent reliability trust bonus up to `+5 pts`) paired with Gemini human-readable explanations.
- **Hardened Food Safety Screening:** Deterministic hot-holding / perishability thresholds and spoilage hazard keyword checks establish a strict minimum risk floor (`REVIEW_REQUIRED` / `HIGH_RISK` / `EXPIRED`) that AI never downgrades.
- **Role-Aware Conversational Chatbot & Public FAQ:** Authenticated `/api/ai/chat` (persistent MongoDB history) and guest `/api/ai/public/chat`.
- **Empirical Analytics Synthesizer:** Real MongoDB statistics (zero fabricated multipliers; per-claim idempotent `ImpactRecord` tracking at `0.40 kg/meal` and `2.5 kg CO2e/kg`).

### 3. Security & Hardened RBAC
- **Four Distinct Roles:** `ROLE_DONOR`, `ROLE_RECEIVER`, `ROLE_VOLUNTEER`, `ROLE_ADMIN` (public registration cannot self-assign `ROLE_ADMIN`).
- **Suspended Account Enforcement:** `JwtAuthenticationFilter` rejects suspended (`active=false`) user JWTs on every request.
- **Ownership & Privacy Enforcement:** Donor-only match inspection, notification owner verification, and automatic masking of donor phone PII on unauthenticated public discovery endpoints (`/api/donations/public/**`).

---

## Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 18, TypeScript 5.4, Vite 5.4, Tailwind CSS 3.4, React Router v6, Leaflet & React-Leaflet, Recharts, Lucide Icons, Axios, Vitest + React Testing Library |
| **Backend** | Java 21, Spring Boot 3.2.5, Spring Security 6 (Stateless JWT + BCrypt), Spring Data MongoDB, ZXing 3.5.3 QR Generator, Spring Boot Actuator, SpringDoc OpenAPI 3 |
| **Database** | MongoDB 6.0+ (auto-connects `localhost:27017` or falls back to embedded in-JVM `mongo-java-server`) |
| **AI Layer** | Google Gemini 1.5 Flash API + Deterministic Fallback Engine |

---

## Quickstart & Automated Tests

### 1. Run the Spring Boot Backend:
```bash
cd backend
mvn spring-boot:run
```
> Backend runs on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`. See `PROJECT_AUDIT.md` for pre-upgrade audit & security remediation history.

### 2. Run the React Frontend:
```bash
cd frontend
npm install
npm run dev
```
> Frontend runs on `http://localhost:3000` (with `/api` proxied to `http://localhost:8080`).

### 3. Run Automated Tests:
```bash
# Backend JUnit Suite (34 tests across 11 classes)
cd backend && mvn test

# Frontend Vitest Suite (10 tests across 5 test files) + Production Build
cd frontend && npm test -- --run && npm run build
```

---

## Demo Accounts (Instant 1-Click Login)

The login page (`/login`) provides 1-click quick login buttons for instant testing:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@annadhata.org` | `admin123` |
| **Donor (Restaurant)** | `spicegarden@restaurant.com` | `donor123` |
| **Donor 2 (Hotel / Catering)** | `royalpalace@hotel.com` | `donor123` |
| **Receiver (NGO Shelter)** | `hope@shelter.org` | `receiver123` |
| **Receiver 2 (Community Kitchen)** | `annaseva@charity.org` | `receiver123` |
| **Volunteer Courier** | `volunteer@annadhata.org` | `volunteer123` |

---

## Complete Documentation
- [Audit & Security Fixes Report](PROJECT_AUDIT.md)
- [REST API Documentation](documentation/API_DOCUMENTATION.md)
- [Database Schema & Indexes](documentation/DATABASE_SCHEMA.md)
- [System Architecture & Algorithms](documentation/PROJECT_ARCHITECTURE.md)
- [Setup & Execution Guide](documentation/SETUP_GUIDE.md)
