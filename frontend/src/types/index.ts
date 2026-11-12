export type Role = 'ROLE_DONOR' | 'ROLE_RECEIVER' | 'ROLE_VOLUNTEER' | 'ROLE_ADMIN';

export type FoodCategory =
  | 'Cooked food'
  | 'Rice and grains'
  | 'Vegetables'
  | 'Fruits'
  | 'Bread and bakery'
  | 'Packaged food'
  | 'Dairy products'
  | 'Other';

export type DonationStatus =
  | 'AVAILABLE'
  | 'MATCHED'
  | 'PARTIALLY_CLAIMED'
  | 'CLAIMED'
  | 'PICKUP_SCHEDULED'
  | 'PICKED_UP'
  | 'COMPLETED'
  | 'EXPIRED'
  | 'CANCELLED';

export type ClaimStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'PICKED_UP'
  | 'REJECTED'
  | 'CANCELLED'
  | 'RELEASED'
  | 'COMPLETED';

export type SafetyIndicator =
  | 'LOW_RISK'
  | 'REVIEW_REQUIRED'
  | 'HIGH_RISK'
  | 'EXPIRED';

export type VerificationStatus =
  | 'PENDING'
  | 'VERIFIED'
  | 'REJECTED'
  | 'SUSPENDED';

export type PassportStatus = 'ACTIVE' | 'VERIFIED_PICKUP' | 'EXPIRED' | 'REVOKED';

export type RescueRelayStatus =
  | 'NONE'
  | 'MISSED_DETECTED'
  | 'AWAITING_DONOR_APPROVAL'
  | 'RELEASED'
  | 'DISMISSED';

export type VolunteerAssignmentStatus =
  | 'UNASSIGNED'
  | 'REQUESTED'
  | 'ASSIGNED'
  | 'PICKED_UP'
  | 'DELIVERED';

export type NotificationType =
  | 'NEW_DONATION'
  | 'MATCH_RECOMMENDATION'
  | 'CLAIM_REQUEST'
  | 'CLAIM_APPROVED'
  | 'CLAIM_REJECTED'
  | 'PICKUP_SCHEDULED'
  | 'PICKUP_REMINDER'
  | 'PASSPORT_VERIFIED'
  | 'RESCUE_RELAY_ALERT'
  | 'VOLUNTEER_ASSIGNED'
  | 'VOLUNTEER_DELIVERED'
  | 'DONATION_COMPLETED'
  | 'EXPIRY_WARNING';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  phone?: string;
  address?: string;
  organizationName?: string;
  organizationType?: string;
  registrationNumber?: string;
  verificationStatus: VerificationStatus;
  verificationDocuments?: string[];
  latitude?: number;
  longitude?: number;
  trustScore?: number;
  trustTier?: string;
  active: boolean;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: string;
  name: string;
  email: string;
  role: Role;
  organizationName?: string;
  organizationType?: string;
  verificationStatus: VerificationStatus;
  latitude?: number;
  longitude?: number;
  address?: string;
}

export interface Donation {
  id: string;
  donorId: string;
  donorName: string;
  donorPhone?: string;
  foodName: string;
  foodDescription?: string;
  foodCategory: FoodCategory;
  quantity: number;
  remainingQuantity?: number;
  allocatedQuantity?: number;
  allocationPercentage?: number;
  allocationCount?: number;
  measurementUnit: string;
  foodImage?: string;
  preparationDateTime: string;
  pickupDeadline: string;
  pickupAddress: string;
  latitude?: number;
  longitude?: number;
  foodHandlingDetails?: string;
  storageInformation?: string;
  status: DonationStatus;
  safetyIndicator: SafetyIndicator;
  safetyNotes?: string;
  claimedByReceiverId?: string;
  claimedByReceiverName?: string;
  distanceKm?: number;
  donorTrustScore?: number;
  donorTrustTier?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ReceiverNeed {
  id: string;
  receiverId: string;
  receiverName: string;
  requiredCategory: FoodCategory;
  requiredQuantity: number;
  measurementUnit: string;
  preferredFoodType?: string;
  numberOfBeneficiaries?: number;
  maxReceivingCapacity?: number;
  requiredDate?: string;
  pickupAvailable: boolean;
  pickupWindowStart?: string;
  pickupWindowEnd?: string;
  latitude?: number;
  longitude?: number;
  address?: string;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface Claim {
  id: string;
  donationId: string;
  donorId: string;
  donorName?: string;
  donorPhone?: string;
  pickupAddress?: string;
  donorLatitude?: number;
  donorLongitude?: number;
  pickupDeadline?: string;
  receiverId: string;
  receiverName: string;
  receiverPhone?: string;
  receiverAddress?: string;
  foodName?: string;
  claimedQuantity: number;
  measurementUnit: string;
  proposedPickupTime?: string;
  confirmedPickupTime?: string;
  status: ClaimStatus;
  notes?: string;
  rejectionReason?: string;
  passportId?: string;
  passportCode?: string;
  qrVerified?: boolean;
  qrVerifiedAt?: string;
  rescueRelayStatus?: RescueRelayStatus;
  missedPickupDetectedAt?: string;
  releasedAt?: string;
  rescueRelayNotes?: string;
  volunteerRequested?: boolean;
  volunteerAssignmentStatus?: VolunteerAssignmentStatus;
  assignedVolunteerId?: string;
  assignedVolunteerName?: string;
  assignedVolunteerPhone?: string;
  receiverTrustScore?: number;
  receiverTrustTier?: string;
  pickedUpAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface MatchRecommendation {
  donationId: string;
  receiverId: string;
  receiverName: string;
  receiverPhone?: string;
  receiverAddress?: string;
  receiverLatitude?: number;
  receiverLongitude?: number;
  distanceKm: number;
  matchScore: number;
  distanceScore: number;
  quantityScore: number;
  urgencyScore: number;
  foodCompatibilityScore: number;
  receiverTrustScore?: number;
  receiverTrustTier?: string;
  trustBonusPoints?: number;
  recommendationExplanation: string;
}

export interface NotificationItem {
  id: string;
  recipientUserId: string;
  title: string;
  message: string;
  type: NotificationType;
  relatedEntityId?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export interface ImpactSummary {
  totalFoodDonatedKg: number;
  totalFoodRedistributedKg: number;
  totalCompletedDonations: number;
  estimatedMealsSaved: number;
  estimatedCo2AvoidedKg: number;
  successRatePercent: number;
  activeDonorsCount: number;
  activeReceiversCount: number;
  categoryRedistributedKg: Record<string, number>;
  monthlyTrends: Array<{
    month: string;
    foodKg: number;
    mealsSaved: number;
  }>;
  mealEstimationFactor: string;
  co2EstimationFactor: string;
  disclaimer: string;
}

export interface DonorDashboard {
  totalDonations: number;
  activeDonations: number;
  completedDonations: number;
  pendingClaims: number;
  totalFoodDonatedKg: number;
  totalFoodRedistributedKg: number;
  estimatedMealsSaved: number;
  estimatedCo2AvoidedKg: number;
  trustScore?: number;
  trustTier?: string;
  activeRescueRelaysCount?: number;
  recentDonations: Donation[];
  recentPendingClaims?: Claim[];
  donationsByCategory?: Record<string, number>;
}

export interface ReceiverDashboard {
  availableNearbyDonations: number;
  activeClaimsCount: number;
  pendingPickupsCount: number;
  completedPickupsCount: number;
  totalQuantityReceivedKg: number;
  estimatedMealsDistributed: number;
  trustScore?: number;
  trustTier?: string;
  recommendedDonations: Donation[];
  currentNeeds: ReceiverNeed[];
}

export interface AdminDashboard {
  totalUsers: number;
  totalDonors: number;
  totalReceivers: number;
  totalVolunteers?: number;
  verifiedOrganisations: number;
  pendingVerificationRequests: number;
  activeRescueRelaysCount?: number;
  totalDonations: number;
  availableDonations: number;
  completedDonations: number;
  expiredDonations: number;
  totalFoodRedistributedKg: number;
  estimatedMealsSaved: number;
  estimatedCo2AvoidedKg: number;
  redistributionSuccessRate: number;
  foodCategoryDistribution?: Record<string, number>;
  flaggedSafetyDonations?: Donation[];
  pendingVerifications?: User[];
}

export interface FoodPassport {
  id: string;
  claimId: string;
  donationId: string;
  donorId: string;
  receiverId: string;
  donorName: string;
  receiverName: string;
  foodName: string;
  allocatedQuantity: number;
  measurementUnit: string;
  passportCode: string;
  hmacSignature: string;
  qrPayloadJson: string;
  qrImageBase64: string;
  status: PassportStatus;
  safetyIndicator: SafetyIndicator;
  issuedAt: string;
  expiresAt: string;
  verifiedAt?: string;
  verifiedByUserId?: string;
  verifiedByName?: string;
  verificationNotes?: string;
}

export interface RescueRelayEvent {
  id: string;
  claimId: string;
  donationId: string;
  donorId: string;
  originalReceiverId: string;
  originalReceiverName: string;
  foodName: string;
  quantityAtRisk: number;
  measurementUnit: string;
  status: RescueRelayStatus;
  confirmedPickupTimeSnapshot?: string;
  pickupDeadlineSnapshot?: string;
  detectedAt: string;
  donorNotifiedAt?: string;
  resolvedAt?: string;
  resolutionNotes?: string;
  alternativeReceivers?: MatchRecommendation[];
}

export interface HeatmapZone {
  zoneName: string;
  centerLat: number;
  centerLon: number;
  totalSurplusKg: number;
  totalDemandKg: number;
  fulfilledKg: number;
  expiredKg: number;
  deficitKg: number;
  surplusIntensity: number;
  demandIntensity: number;
  gapIntensity: number;
  donationCount: number;
  needCount: number;
  topSurplusCategory: string;
  topDemandCategory: string;
  recommendedAction: string;
}

export interface DemandHeatmap {
  timeRange: string;
  categoryFilter: string;
  totalHistoricalSurplusKg: number;
  totalActiveDemandKg: number;
  totalFulfilledKg: number;
  totalExpiredKg: number;
  peakSurplusHours: string;
  highestDemandZone: string;
  highestSurplusZone: string;
  aiSpatialInsight: string;
  zones: HeatmapZone[];
  demandVsSurplusByCategory: Record<string, number>;
}

export interface VolunteerProfile {
  id: string;
  userId: string;
  userName: string;
  phone?: string;
  vehicleType: string;
  maxCapacityKg: number;
  serviceRadiusKm: number;
  availableForDispatch: boolean;
  latitude?: number;
  longitude?: number;
  address?: string;
  completedDeliveriesCount: number;
  totalKgTransported: number;
}

export interface VolunteerTask {
  claimId: string;
  donationId: string;
  foodName: string;
  foodCategory: FoodCategory;
  claimedQuantity: number;
  measurementUnit: string;
  claimStatus: ClaimStatus;
  volunteerAssignmentStatus: VolunteerAssignmentStatus;
  volunteerRequested: boolean;
  donorId: string;
  donorName: string;
  donorPhone?: string;
  pickupAddress: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  receiverId: string;
  receiverName: string;
  receiverPhone?: string;
  dropoffAddress?: string;
  dropoffLatitude?: number;
  dropoffLongitude?: number;
  distanceFromVolunteerKm?: number;
  routeDistanceKm?: number;
  pickupDeadline?: string;
  confirmedPickupTime?: string;
  passportCode?: string;
  qrVerified: boolean;
  assignedVolunteerId?: string;
  assignedVolunteerName?: string;
  assignedVolunteerPhone?: string;
}

export interface VolunteerDashboard {
  openTasksCount: number;
  activeMissionsCount: number;
  completedDeliveriesCount: number;
  totalKgTransported: number;
  estimatedMealsDelivered: number;
  trustScore: number;
  trustTier: string;
  volunteerProfile: VolunteerProfile;
  activeMissions: VolunteerTask[];
  openTasks: VolunteerTask[];
}

export interface TrustFactor {
  factorName: string;
  weightPercent: number;
  score: number;
  detail: string;
}

export interface TrustScore {
  userId: string;
  userName: string;
  role: Role;
  overallScore: number;
  tier: string;
  factors: TrustFactor[];
  completedCount: number;
  totalTransactions: number;
  onTimePickups: number;
  missedPickups: number;
  cancelledCount: number;
  safetyFlagsCount: number;
  explanation: string;
  calculatedAt: string;
}

export interface AiExtractionResponse {
  foodName: string;
  quantity: number;
  unit: string;
  foodCategory: FoodCategory;
  preparationDateTime: string;
  pickupDeadline: string;
  foodDescription: string;
  storageAdvice: string;
  rawExtractionNotes: string;
}

export interface AiChatResponse {
  reply: string;
  quickSuggestions: string[];
  relevantActions: string[];
  timestamp: string;
}

export interface AiAnalyticsReport {
  executiveSummary: string;
  redistributionSuccessRate: number;
  totalRedistributedKg: number;
  totalCompletedDonations: number;
  totalExpiredDonations: number;
  peakDonationHours: string;
  highestSurplusCategory: string;
  highestDeficitCategory: string;
  strategicInsights: string[];
  actionableRecommendations: string[];
  categoryDistribution?: Record<string, number>;
  generatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}
