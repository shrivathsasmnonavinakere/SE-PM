# AnnaDhata MongoDB Database Schema & Indexing Guide

**Smart Food Waste Reduction and Sustainable Redistribution Ecosystem**
*Database Engine:* MongoDB 6.0+ (auto-connects `localhost:27017` with automatic embedded in-JVM `mongo-java-server` fallback if port `27017` is unavailable).

---

## 1. Overview of Collections

AnnaDhata persists all platform workflows in 14 MongoDB collections across core redistribution, verification, security auditing, QR Passports, Rescue Relay, Volunteer Network, and Trust Scoring:

| Collection Name | Associated Model Class | Purpose |
|---|---|---|
| `users` | `com.annadhata.model.User` | Donors, Receivers (NGOs), Volunteers (`ROLE_VOLUNTEER`), and Admins |
| `donations` | `com.annadhata.model.Donation` | Surplus listings with partial quantity tracking (`remainingQuantity`, `allocatedQuantity`) |
| `receiver_needs` | `com.annadhata.model.ReceiverNeed` | Ongoing food demand profiles, capacities, and pickup windows |
| `claims` | `com.annadhata.model.Claim` | Partial/full portion reservations, passport refs, relay status, and volunteer assignments |
| `foodPassports` | `com.annadhata.model.FoodPassport` | HMAC-SHA256 signed QR Food Passports + Base64 PNG QR codes |
| `rescueRelayEvents` | `com.annadhata.model.RescueRelayEvent` | Missed pickup detection events, release approvals, and alternative NGO match logs |
| `volunteerProfiles` | `com.annadhata.model.VolunteerProfile` | Volunteer vehicle types, carrying capacities, service radii, and dispatch status |
| `trustScores` | `com.annadhata.model.TrustScore` | Computed behavioral reliability scores and transparent `TrustFactor` arrays |
| `impact_records` | `com.annadhata.model.ImpactRecord` | Per-claim idempotent sustainability records (`claimId` unique sparse/indexed) |
| `notifications` | `com.annadhata.model.Notification` | Persistent user alerts (claims, QR handovers, Rescue Relay, volunteer dispatch) |
| `verification_requests` | `com.annadhata.model.VerificationRequest` | NGO registration numbers, uploaded document URLs, and Admin review decisions |
| `match_results` | `com.annadhata.model.MatchResult` | Cached smart matching scores and Gemini AI explanations |
| `chat_history` | `com.annadhata.model.ChatHistory` | Role-aware AI chatbot conversation logs |
| `audit_logs` | `com.annadhata.model.AuditLog` | Administrative verification, suspension, and moderation trails |

---

## 2. Updated & New Schema Structures

### 2.1 `users` Extensions
In addition to identity, contact, coordinates, `verificationStatus`, `verificationDocuments`, and `active`:
- `role`: `"ROLE_DONOR" | "ROLE_RECEIVER" | "ROLE_VOLUNTEER" | "ROLE_ADMIN"`
- `trustScore`: `Double` (0.0 – 100.0 cached summary)
- `trustTier`: `"PLATINUM" | "GOLD" | "SILVER" | "NEEDS_IMPROVEMENT"`

### 2.2 `donations` (Partial Food Distribution Fields)
**Compound Indexes:**
- `{'status': 1, 'pickupDeadline': 1}` (`status_deadline_idx`)
- `{'latitude': 1, 'longitude': 1, 'status': 1}` (`location_status_idx`)

**Key Fields:**
- `quantity` (`Double`), `remainingQuantity` (`Double`), `allocatedQuantity` (`Double`), `allocationCount` (`Integer`), `activeClaimIds` (`List<String>`)
- `status`: `"AVAILABLE" | "MATCHED" | "PARTIALLY_CLAIMED" | "CLAIMED" | "PICKUP_SCHEDULED" | "PICKED_UP" | "COMPLETED" | "EXPIRED" | "CANCELLED"`
- `@Version`: optimistic concurrency version field alongside atomic `mongoTemplate.findAndModify`.

### 2.3 `claims` Extensions
**Compound Index:** `{'donationId': 1, 'receiverId': 1, 'status': 1}` (`donation_receiver_status_idx`)

**Key Fields:**
- Partial quantity: `claimedQuantity`, `status` (`PENDING`, `APPROVED`, `PICKED_UP`, `REJECTED`, `CANCELLED`, `RELEASED`, `COMPLETED`)
- QR Food Passport link: `passportId`, `passportCode`, `qrVerified`, `qrVerifiedAt`
- Rescue Relay tracking: `rescueRelayStatus` (`NONE`, `MISSED_DETECTED`, `AWAITING_DONOR_APPROVAL`, `RELEASED`, `DISMISSED`), `missedPickupDetectedAt`, `releasedAt`, `rescueRelayNotes`
- Volunteer dispatch: `volunteerRequested`, `volunteerAssignmentStatus` (`UNASSIGNED`, `REQUESTED`, `ASSIGNED`, `PICKED_UP`, `DELIVERED`), `assignedVolunteerId`, `assignedVolunteerName`, `assignedVolunteerPhone`

### 2.4 `foodPassports`
- **Unique Index:** `claimId` (`@Indexed(unique = true)`), plus indexes on `donationId`, `donorId`, `receiverId`, `passportCode`
- Stores `allocatedQuantity`, `passportCode`, `hmacSignature`, `qrPayloadJson`, `qrImageBase64`, `status` (`ACTIVE`, `VERIFIED_PICKUP`, `EXPIRED`, `REVOKED`), `safetyIndicator`, `issuedAt`, `expiresAt`, `verifiedAt`, `verifiedByUserId`, `verifiedByName`.

### 2.5 `rescueRelayEvents`
- **Unique Index:** `claimId` (`@Indexed(unique = true)`), plus indexes on `donationId`, `donorId`, `originalReceiverId`, `status`
- Tracks `foodName`, `quantityAtRisk`, `confirmedPickupTimeSnapshot`, `pickupDeadlineSnapshot`, `alternativeReceiverIdsNotified`, `alternativeMatchSummaries`, `donorApprovedRelease`, `resolvedAt`.

### 2.6 `volunteerProfiles`
- **Unique Index:** `userId`
- Stores `vehicleType`, `maxCapacityKg`, `serviceRadiusKm`, `availableForDispatch`, `latitude`, `longitude`, `address`, `completedDeliveriesCount`, `totalKgTransported`.

### 2.7 `trustScores`
- **Unique Index:** `userId`
- Stores `overallScore`, `tier`, `factors` (list of `{ factorName, weightPercent, score, detail }`), `completedCount`, `totalTransactions`, `onTimePickups`, `missedPickups`, `cancelledCount`, `safetyFlagsCount`, `explanation`, `calculatedAt`.

### 2.8 `impact_records`
- Tracks `claimId` alongside `donationId`, `donorId`, `receiverId`, `foodCategory`, `quantitySavedKg`, `estimatedMealsSaved`, `estimatedCo2AvoidedKg` so multiple partial claim completions from a single donation are recorded individually without duplication.

---

## 3. Atomic Concurrency Invariants

1. **Atomic Partial Allocation (`ClaimService.approveClaim`):**
   Uses `mongoTemplate.findAndModify` requiring `status in [AVAILABLE, MATCHED, PARTIALLY_CLAIMED]`, `pickupDeadline > now`, and `remainingQuantity >= claimedQuantity`, atomically updating `remainingQuantity`, `allocatedQuantity`, `allocationCount`, and `activeClaimIds`.
2. **Atomic Volunteer Dispatch (`VolunteerService.acceptTask`):**
   Uses `mongoTemplate.findAndModify` verifying `status == APPROVED`, `assignedVolunteerId == null`, and `volunteerAssignmentStatus in [null, UNASSIGNED, REQUESTED]` before assigning `assignedVolunteerId`, preventing double courier assignment.
3. **Atomic Rescue Relay Quantity Restoration (`RescueRelayService`):**
   Upon donor-approved release, atomically re-credits `remainingQuantity += quantityAtRisk` and recalculates listing status (`PARTIALLY_CLAIMED` or `AVAILABLE`).
4. **Automated Schedulers (`@Scheduled(fixedRate = 60000)`):**
   - `DonationService`: Expires overdue `AVAILABLE`, `MATCHED`, and `PARTIALLY_CLAIMED` listings.
   - `RescueRelayService`: Scans overdue `APPROVED` pickups exceeding `15-minute` grace windows and triggers `AWAITING_DONOR_APPROVAL` events.
