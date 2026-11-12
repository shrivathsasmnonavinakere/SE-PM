# AnnaDhata - Complete Setup & Execution Guide

**Smart Food Waste Reduction and Sustainable Redistribution Ecosystem**
*Tagline: "Turning Surplus Food Into Meals, Not Waste."*

---

## Prerequisites
- **Java Development Kit (JDK):** Version 21
- **Node.js:** v18.0.0 or higher
- **Maven:** v3.8+
- **MongoDB (Optional):** Connects to `localhost:27017` when running; otherwise automatically starts an in-JVM `mongo-java-server` fallback on port `27017` with zero manual setup. Note: Existing pre-upgrade MongoDB databases automatically receive idempotent `DataInitializer` backfills on startup (adding `remainingQuantity`, the demo volunteer account, and initial Trust Scores).

---

## 1. Quickstart Commands

### Step 1: Run the Backend
```bash
cd backend
mvn spring-boot:run
```
Or package and run the standalone JAR:
```bash
cd backend
mvn package -DskipTests
java -jar target/annadhata-backend-1.0.0.jar
```
Backend starts on **`http://localhost:8080`**.
On first run, `DataInitializer` seeds:
- Admin (`admin@annadhata.org`)
- 2 Verified Donors (`spicegarden@restaurant.com`, `royalpalace@hotel.com`)
- 2 Verified NGOs (`hope@shelter.org`, `annaseva@charity.org`)
- 1 Verified Volunteer Courier (`volunteer@annadhata.org`) + `VolunteerProfile`
- Active surplus listings, a `PARTIALLY_CLAIMED` 40 kg catering showcase (`15 kg` approved with QR Food Passport & open volunteer mission, `25 kg` remaining), historical completed impact metrics, Whitefield expired heatmap data, and computed Trust Scores.

### Step 2: Run the Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend starts on **`http://localhost:3000`** with `/api` proxied to `http://localhost:8080` (configured in `frontend/vite.config.ts`).

---

## 2. Pre-Configured Demo Credentials

The login page (`http://localhost:3000/login`) provides 1-click Demo Login buttons:

| Role | Email | Password | Capabilities |
|---|---|---|---|
| **ADMIN** | `admin@annadhata.org` | `admin123` | Organization verifications, safety moderation (`/admin/moderation`), AI analytics (`/admin/analytics`), Rescue Relay & Heatmap oversight |
| **DONOR** | `spicegarden@restaurant.com` | `donor123` | Post surplus / AI extractor, view NGO matches + trust badges, approve partial claims, verify QR Passports, manage Rescue Relay |
| **DONOR 2** | `royalpalace@hotel.com` | `donor123` | Owner of the `PARTIALLY_CLAIMED` 40 kg catering demo listing |
| **RECEIVER** | `hope@shelter.org` | `receiver123` | Discover full & partial donations on list/map, claim portions, request volunteer couriers, inspect QR Food Passport |
| **RECEIVER 2** | `annaseva@charity.org` | `receiver123` | Holds the 15 kg approved partial split + active QR Food Passport |
| **VOLUNTEER** | `volunteer@annadhata.org` | `volunteer123` | Open dispatch missions (`/volunteer/dashboard`), configure vehicle/capacity/radius, verify QR Passports (`/verify-passport`), confirm NGO dropoff |

---

## 3. Environment Variables

### Backend (system environment or JVM args):
- `MONGODB_URI` (default: `mongodb://localhost:27017/annadhata`)
- `JWT_SECRET` (override default dev key in production)
- `GEMINI_API_KEY` (optional; deterministic AI fallbacks engage cleanly if omitted)
- `GEMINI_MODEL` (default: `gemini-1.5-flash`)

---

## 4. Testing & Build Verification

### Backend JUnit Suite (`34` tests across 11 classes):
```bash
cd backend
mvn test
```
Covers `ClaimServiceTest` (partial allocation & concurrency), `PassportServiceTest` (HMAC signing, QR PNG generation, verification & revocation), `RescueRelayServiceTest`, `VolunteerServiceTest`, `TrustScoreServiceTest`, `HeatmapServiceTest`, `MatchingServiceTest`, `FoodSafetyServiceTest`, `GeminiAIServiceTest`, `SecurityAndAiUpgradeTest`, and `GeoUtilsTest`.

*Windows Tip:* `backend/pom.xml` configures `-javaagent` for Byte Buddy (`byte-buddy-agent`) on Surefire so Mockito works smoothly with Java 21 on Windows.

### Frontend Vitest Suite (`10` tests across 5 files):
```bash
cd frontend
npm test -- --run
```
Covers local timezone formatting (`datetime.test.ts`), `TrustBadge` breakdown rendering, `QrPassportModal` display + verification, `ProtectedRoute` + Volunteer quick login redirect, and `AvailableDonationsPage` partial distribution caps & unverified NGO blocks.

### Production Frontend Bundle:
```bash
cd frontend
npm run build
```
Runs `tsc && vite build` outputting optimized assets to `frontend/dist`.
