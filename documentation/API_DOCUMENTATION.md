# AnnaDhata REST API Documentation

**Smart Food Waste Reduction and Sustainable Redistribution Ecosystem**
*Base URL:* `http://localhost:8080/api`
*Swagger / OpenAPI UI:* `http://localhost:8080/swagger-ui.html`
*OpenAPI Spec:* `http://localhost:8080/v3/api-docs`

All responses are wrapped in `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2026-11-12T..."
}
```

---

## Table of Contents
1. [Authentication & Profile](#1-authentication--profile)
2. [Food Donation Management](#2-food-donation-management)
3. [Receiver Needs](#3-receiver-needs)
4. [Partial Claim & Pickup Lifecycle](#4-partial-claim--pickup-lifecycle)
5. [QR Food Passport Verification](#5-qr-food-passport-verification)
6. [Rescue Relay Missed-Pickup Recovery](#6-rescue-relay-missed-pickup-recovery)
7. [Volunteer Network Dispatch](#7-volunteer-network-dispatch)
8. [Trust & Reliability Score](#8-trust--reliability-score)
9. [AI Services & Demand Heatmap](#9-ai-services--demand-heatmap)
10. [Impact, Notifications & Dashboards](#10-impact-notifications--dashboards)
11. [Admin Operations & Public Seed](#11-admin-operations--public-seed)

---

## 1. Authentication & Profile

All protected endpoints expect:
```http
Authorization: Bearer <jwt-token>
```

- `POST /api/auth/register` *(Public)*: Register `ROLE_DONOR`, `ROLE_RECEIVER` (with optional `verificationDocuments` array), or `ROLE_VOLUNTEER` (with optional `vehicleType`, `maxCapacityKg`, `serviceRadiusKm`). Self-registration as `ROLE_ADMIN` returns `403 Forbidden`.
- `POST /api/auth/login` *(Public)*: Authenticate and receive JWT + profile summary (`AuthResponse`).
- `GET /api/auth/me` *(Authenticated)*: Retrieve current user profile including `trustScore` and `trustTier`.
- `PUT /api/auth/profile` *(Authenticated)*: Update contact details, coordinates, organization info, and (for Receivers) `verificationDocuments`.

---

## 2. Food Donation Management

- `POST /api/donations` *(`ROLE_DONOR`, `ROLE_ADMIN`)*: Create food listing, initialize `remainingQuantity = quantity`, run safety screening, and compute top NGO matches.
- `PUT /api/donations/{id}` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Update listing details; prevents reducing `quantity` below `allocatedQuantity`.
- `DELETE /api/donations/{id}` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Cancel donation and cascade-cancel active claims + revoke active QR Passports.
- `GET /api/donations/{id}` *(Authenticated)*: Full donation details (with partial allocation metrics, optional `?lat=&lon=`).
- `GET /api/donations/available` *(Authenticated)*: Active discoverable donations (`AVAILABLE`, `MATCHED`, `PARTIALLY_CLAIMED` where `remainingQuantity > 0`), filterable by `?category=&lat=&lon=&maxDistanceKm=`. Note `category` accepts enum names or display strings (e.g., `Cooked food`).
- `GET /api/donations/public/available` & `GET /api/donations/public/{id}` *(Public)*: Public browse endpoints with `donorPhone` masked (`null`).
- `GET /api/donations/my` *(`ROLE_DONOR`, `ROLE_ADMIN`)*: Authenticated donor's own listings.
- `GET /api/donations/{id}/matches` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Top 5 ranked NGO match recommendations including `receiverTrustScore`, `receiverTrustTier`, `trustBonusPoints`, and Gemini rationale.

---

## 3. Receiver Needs

- `POST /api/needs` *(`ROLE_RECEIVER`)*: Create/update receiver dietary requirement profile.
- `GET /api/needs/my` *(`ROLE_RECEIVER`)*: Get authenticated receiver's needs.
- `GET /api/needs/active` *(Authenticated)*: List active needs across verified NGOs.
- `DELETE /api/needs/{id}` *(`ROLE_RECEIVER` owner, `ROLE_ADMIN`)*: Deactivate/remove requirement.

---

## 4. Partial Claim & Pickup Lifecycle

Supports multiple verified NGOs claiming different portions of a single surplus listing without overselling `remainingQuantity`.

- `POST /api/claims` *(`ROLE_RECEIVER` verified, `ROLE_ADMIN`)*: Submit partial or full claim:
  ```json
  {
    "donationId": "...",
    "claimedQuantity": 15.0,
    "proposedPickupTime": "2026-11-12T15:00:00Z",
    "notes": "Insulated boxes on standby",
    "volunteerRequested": true
  }
  ```
- `PUT /api/claims/{id}/approve` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Executes atomic MongoDB `findAndModify` checking `remainingQuantity >= claimedQuantity`, decrements `remainingQuantity`, increments `allocatedQuantity` & `allocationCount`, transitions listing to `PARTIALLY_CLAIMED` (or `CLAIMED` when remaining == 0), and issues an HMAC-signed QR Food Passport.
- `PUT /api/claims/{id}/reject` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Reject pending claim.
- `PUT /api/claims/{id}/picked-up` *(Donor/Receiver participant, `ROLE_ADMIN`)*: Mark allocated portion in transit (`PICKED_UP`).
- `PUT /api/claims/{id}/complete` *(Donor/Receiver participant, `ROLE_ADMIN`)*: Finalize redistribution, record idempotent per-claim `ImpactRecord`, update donation status when all active splits complete, and recalculate participant Trust Scores.
- `GET /api/claims/my` *(Authenticated)*: Receiver claims or Donor incoming claims.
- `GET /api/claims/donation/{donationId}` *(Authenticated)*: Claims tied to a listing.

---

## 5. QR Food Passport Verification

- `GET /api/passports/claim/{claimId}` *(Authenticated participant / assigned volunteer / Admin)*: Retrieve `FoodPassport` with `passportCode` (e.g., `AD-F7CF-B249-49F6`), `hmacSignature`, signed `qrPayloadJson`, and 260x260 Base64 PNG `qrImageBase64`.
- `POST /api/passports/verify` *(`ROLE_DONOR`, `ROLE_VOLUNTEER`, `ROLE_ADMIN`)*:
  ```json
  {
    "passportCode": "AD-F7CF-B249-49F6",
    "verificationNotes": "Verified seal and container count at pickup gate"
  }
  ```
  Validates expiry, status (`ACTIVE`), and HMAC signature; transitions passport to `VERIFIED_PICKUP` and claim to `PICKED_UP`.

---

## 6. Rescue Relay Missed-Pickup Recovery

- `GET /api/rescue-relay/my` *(Authenticated)*: Fetch Rescue Relay alerts scoped to Donor, Receiver, or Admin (`all`).
- `POST /api/rescue-relay/scan?graceMinutes=15` *(`ROLE_DONOR`, `ROLE_ADMIN`)*: Scan approved claims past `confirmedPickupTime + graceMinutes` (also runs automatically every 60s via `@Scheduled`), creating `AWAITING_DONOR_APPROVAL` events and notifying donor & receiver.
- `POST /api/rescue-relay/claims/{claimId}/release` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: With optional `{ "extendDeadlineMinutes": 60, "notes": "..." }`, transitions claim to `RELEASED`, revokes its QR Passport, atomically restores `remainingQuantity`, updates Trust Scores, and runs alternative NGO matching (`excludedReceiverIds`) to notify substitute NGOs.
- `POST /api/rescue-relay/claims/{claimId}/dismiss` *(`ROLE_DONOR` owner, `ROLE_ADMIN`)*: Dismiss alert and retain existing pickup arrangement.

---

## 7. Volunteer Network Dispatch

- `GET /api/volunteers/dashboard` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Aggregated volunteer KPIs, active missions, open pickup tasks, and reliability score.
- `GET /api/volunteers/profile` & `PUT /api/volunteers/profile` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Configure `vehicleType`, `maxCapacityKg`, `serviceRadiusKm`, `availableForDispatch`, and base coordinates.
- `GET /api/volunteers/tasks/available` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Approved unassigned deliveries sorted by proximity/deadline.
- `GET /api/volunteers/tasks/my` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Missions assigned to or completed by the caller.
- `POST /api/volunteers/tasks/{claimId}/accept` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Atomic claim-level courier assignment (`ASSIGNED`).
- `POST /api/volunteers/tasks/{claimId}/release` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Release mission before pickup.
- `POST /api/volunteers/tasks/{claimId}/deliver` *(`ROLE_VOLUNTEER`, `ROLE_ADMIN`)*: Complete volunteer delivery (`DELIVERED`), finalize claim & idempotent impact metrics, and refresh Trust Scores.
- `POST /api/volunteers/claims/{claimId}/request` *(`ROLE_DONOR`, `ROLE_RECEIVER`, `ROLE_ADMIN`)*: Flag an approved claim for volunteer courier dispatch.

---

## 8. Trust & Reliability Score

- `GET /api/trust/me` *(Authenticated)*: Current user's transparent score, tier, transaction tallies, and weighted `TrustFactor` array.
- `GET /api/trust/{userId}` *(Authenticated)*: View behavioral trust profile and factor breakdown for any Donor, Receiver, or Volunteer.
- `POST /api/trust/{userId}/recalculate` *(`ROLE_ADMIN`)*: Force recalculation from MongoDB ledger records.

---

## 9. AI Services & Demand Heatmap

- `POST /api/ai/extract-donation` *(`ROLE_DONOR`, `ROLE_ADMIN`)*: Natural-language prompt `{ "text": "..." }` -> structured JSON draft (`foodName`, `quantity`, `unit`, `foodCategory`, `preparationDateTime`, `pickupDeadline`, etc.). Never auto-publishes.
- `POST /api/ai/screen-safety` *(`ROLE_DONOR`, `ROLE_ADMIN`)*: Food safety pre-screening with deterministic minimum severity enforcement.
- `POST /api/ai/chat` *(Authenticated)* & `GET /api/ai/chat-history` *(Authenticated)*: Role-aware assistant with MongoDB chat history.
- `POST /api/ai/public/chat` *(Public)*: Stateless FAQ onboarding assistant for logged-out visitors.
- `GET /api/ai/analytics-report` *(`ROLE_ADMIN`)*: Executive analytics synthesized from real MongoDB metrics.
- `GET /api/analytics/heatmap?timeRange=all&category=` *(Authenticated)*: Spatial 2.2 km grid clusters comparing historical demand, active need capacity, surplus donated, and expired waste (`DemandHeatmapResponse`), paired with Gemini spatial insight.

---

## 10. Impact, Notifications & Dashboards

- `GET /api/impact/summary` *(Authenticated)* & `GET /api/impact/public/summary` *(Public)*: Aggregated platform sustainability metrics (`0.40 kg/meal`, `2.5 kg CO2e/kg`).
- `GET /api/notifications`, `GET /api/notifications/unread-count`, `PUT /api/notifications/{id}/read`, `PUT /api/notifications/read-all` *(Authenticated; owner-enforced on read mark)*.
- `GET /api/donor/dashboard` *(`ROLE_DONOR`, `ROLE_ADMIN`)*
- `GET /api/receiver/dashboard` *(`ROLE_RECEIVER`, `ROLE_ADMIN`)*
- `GET /api/admin/dashboard` *(`ROLE_ADMIN`)*

---

## 11. Admin Operations & Public Seed

- `GET /api/admin/users` *(`ROLE_ADMIN`)*
- `GET /api/admin/donations` *(`ROLE_ADMIN`)*: Full platform donation moderation & safety queue.
- `PUT /api/admin/users/{id}/verify` *(`ROLE_ADMIN`)*: Approve or reject NGO verification `{ "approve": true, "notes": "..." }`.
- `PUT /api/admin/users/{id}/toggle-active` *(`ROLE_ADMIN`)*: Activate or suspend user account (cannot self-suspend).
- `PUT /api/admin/donations/{id}/moderate` *(`ROLE_ADMIN`)*: Moderate inappropriate/unsafe listings.
- `GET /api/admin/audit-logs` *(`ROLE_ADMIN`)*
- `POST /api/public/seed` *(Public)*: Trigger idempotent seed check.
- `GET /actuator/health`, `GET /actuator/info` *(Public)*: Service health & metadata (`ADMIN`-restricted for remaining Actuator endpoints).
