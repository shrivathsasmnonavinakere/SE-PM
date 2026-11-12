import axios from 'axios';
import {
  ApiResponse,
  AuthResponse,
  User,
  Donation,
  Claim,
  ReceiverNeed,
  MatchRecommendation,
  NotificationItem,
  ImpactSummary,
  DonorDashboard,
  ReceiverDashboard,
  AdminDashboard,
  AiExtractionResponse,
  AiChatResponse,
  AiAnalyticsReport,
  FoodCategory,
  DonationStatus,
  FoodPassport,
  RescueRelayEvent,
  DemandHeatmap,
  VolunteerDashboard,
  VolunteerProfile,
  VolunteerTask,
  TrustScore,
} from '../types';

const API = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('annadhata_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (!error.config.url?.includes('/auth/login')) {
        localStorage.removeItem('annadhata_token');
        localStorage.removeItem('annadhata_user');
      }
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  register: async (data: any): Promise<ApiResponse<AuthResponse>> => {
    const res = await API.post('/auth/register', data);
    return res.data;
  },
  login: async (data: any): Promise<ApiResponse<AuthResponse>> => {
    const res = await API.post('/auth/login', data);
    return res.data;
  },
  me: async (): Promise<ApiResponse<User>> => {
    const res = await API.get('/auth/me');
    return res.data;
  },
  updateProfile: async (data: any): Promise<ApiResponse<User>> => {
    const res = await API.put('/auth/profile', data);
    return res.data;
  },
};

// Donation API
export const donationApi = {
  create: async (data: any): Promise<ApiResponse<Donation>> => {
    const res = await API.post('/donations', data);
    return res.data;
  },
  update: async (id: string, data: any): Promise<ApiResponse<Donation>> => {
    const res = await API.put(`/donations/${id}`, data);
    return res.data;
  },
  cancel: async (id: string): Promise<ApiResponse<Donation>> => {
    const res = await API.delete(`/donations/${id}`);
    return res.data;
  },
  getById: async (id: string, lat?: number, lon?: number): Promise<ApiResponse<Donation>> => {
    const res = await API.get(`/donations/${id}`, { params: { lat, lon } });
    return res.data;
  },
  getPublicById: async (id: string): Promise<ApiResponse<Donation>> => {
    const res = await API.get(`/donations/public/${id}`);
    return res.data;
  },
  getAvailable: async (params?: { category?: FoodCategory; lat?: number; lon?: number; maxDistanceKm?: number }): Promise<ApiResponse<Donation[]>> => {
    const res = await API.get('/donations/available', { params });
    return res.data;
  },
  getPublicAvailable: async (params?: { category?: FoodCategory; lat?: number; lon?: number; maxDistanceKm?: number }): Promise<ApiResponse<Donation[]>> => {
    const res = await API.get('/donations/public/available', { params });
    return res.data;
  },
  getMy: async (): Promise<ApiResponse<Donation[]>> => {
    const res = await API.get('/donations/my');
    return res.data;
  },
  getMatches: async (donationId: string): Promise<ApiResponse<MatchRecommendation[]>> => {
    const res = await API.get(`/donations/${donationId}/matches`);
    return res.data;
  },
};

// Claim API (Supports Partial Food Distribution)
export const claimApi = {
  create: async (data: {
    donationId: string;
    claimedQuantity: number;
    proposedPickupTime?: string;
    notes?: string;
    volunteerRequested?: boolean;
  }): Promise<ApiResponse<Claim>> => {
    const res = await API.post('/claims', data);
    return res.data;
  },
  approve: async (claimId: string): Promise<ApiResponse<Claim>> => {
    const res = await API.put(`/claims/${claimId}/approve`);
    return res.data;
  },
  reject: async (claimId: string, reason?: string): Promise<ApiResponse<Claim>> => {
    const res = await API.put(`/claims/${claimId}/reject`, { reason });
    return res.data;
  },
  markPickedUp: async (claimId: string): Promise<ApiResponse<Claim>> => {
    const res = await API.put(`/claims/${claimId}/picked-up`);
    return res.data;
  },
  markCompleted: async (claimId: string): Promise<ApiResponse<Claim>> => {
    const res = await API.put(`/claims/${claimId}/complete`);
    return res.data;
  },
  getMy: async (): Promise<ApiResponse<Claim[]>> => {
    const res = await API.get('/claims/my');
    return res.data;
  },
  getByDonation: async (donationId: string): Promise<ApiResponse<Claim[]>> => {
    const res = await API.get(`/claims/donation/${donationId}`);
    return res.data;
  },
};

// QR Food Passport API
export const passportApi = {
  getByClaim: async (claimId: string): Promise<ApiResponse<FoodPassport>> => {
    const res = await API.get(`/passports/claim/${claimId}`);
    return res.data;
  },
  verify: async (passportCode: string, verificationNotes?: string): Promise<ApiResponse<FoodPassport>> => {
    const res = await API.post('/passports/verify', { passportCode, verificationNotes });
    return res.data;
  },
};

// Rescue Relay API
export const rescueRelayApi = {
  getMy: async (): Promise<ApiResponse<RescueRelayEvent[]>> => {
    const res = await API.get('/rescue-relay/my');
    return res.data;
  },
  scan: async (graceMinutes: number = 15): Promise<ApiResponse<RescueRelayEvent[]>> => {
    const res = await API.post('/rescue-relay/scan', null, { params: { graceMinutes } });
    return res.data;
  },
  release: async (
    claimId: string,
    payload?: { extendDeadlineMinutes?: number; notes?: string }
  ): Promise<ApiResponse<RescueRelayEvent>> => {
    const res = await API.post(`/rescue-relay/claims/${claimId}/release`, payload || {});
    return res.data;
  },
  dismiss: async (claimId: string, notes?: string): Promise<ApiResponse<RescueRelayEvent>> => {
    const res = await API.post(`/rescue-relay/claims/${claimId}/dismiss`, { notes });
    return res.data;
  },
};

// Volunteer Network API
export const volunteerApi = {
  getDashboard: async (): Promise<ApiResponse<VolunteerDashboard>> => {
    const res = await API.get('/volunteers/dashboard');
    return res.data;
  },
  getProfile: async (): Promise<ApiResponse<VolunteerProfile>> => {
    const res = await API.get('/volunteers/profile');
    return res.data;
  },
  updateProfile: async (data: Partial<VolunteerProfile>): Promise<ApiResponse<VolunteerProfile>> => {
    const res = await API.put('/volunteers/profile', data);
    return res.data;
  },
  getAvailableTasks: async (): Promise<ApiResponse<VolunteerTask[]>> => {
    const res = await API.get('/volunteers/tasks/available');
    return res.data;
  },
  getMyMissions: async (): Promise<ApiResponse<VolunteerTask[]>> => {
    const res = await API.get('/volunteers/tasks/my');
    return res.data;
  },
  acceptTask: async (claimId: string): Promise<ApiResponse<VolunteerTask>> => {
    const res = await API.post(`/volunteers/tasks/${claimId}/accept`);
    return res.data;
  },
  releaseTask: async (claimId: string): Promise<ApiResponse<VolunteerTask>> => {
    const res = await API.post(`/volunteers/tasks/${claimId}/release`);
    return res.data;
  },
  markDelivered: async (claimId: string): Promise<ApiResponse<VolunteerTask>> => {
    const res = await API.post(`/volunteers/tasks/${claimId}/deliver`);
    return res.data;
  },
  requestSupport: async (claimId: string): Promise<ApiResponse<VolunteerTask>> => {
    const res = await API.post(`/volunteers/claims/${claimId}/request`);
    return res.data;
  },
};

// Trust & Reliability Score API
export const trustApi = {
  getMy: async (): Promise<ApiResponse<TrustScore>> => {
    const res = await API.get('/trust/me');
    return res.data;
  },
  getByUser: async (userId: string): Promise<ApiResponse<TrustScore>> => {
    const res = await API.get(`/trust/${userId}`);
    return res.data;
  },
  recalculate: async (userId: string): Promise<ApiResponse<TrustScore>> => {
    const res = await API.post(`/trust/${userId}/recalculate`);
    return res.data;
  },
};

// AI Demand Heatmap API
export const heatmapApi = {
  getDemandHeatmap: async (timeRange: string = 'all', category?: FoodCategory): Promise<ApiResponse<DemandHeatmap>> => {
    const params: any = { timeRange };
    if (category) params.category = category;
    const res = await API.get('/analytics/heatmap', { params });
    return res.data;
  },
};

// Receiver Need API
export const needApi = {
  create: async (data: any): Promise<ApiResponse<ReceiverNeed>> => {
    const res = await API.post('/needs', data);
    return res.data;
  },
  getMy: async (): Promise<ApiResponse<ReceiverNeed[]>> => {
    const res = await API.get('/needs/my');
    return res.data;
  },
  getActive: async (): Promise<ApiResponse<ReceiverNeed[]>> => {
    const res = await API.get('/needs/active');
    return res.data;
  },
  delete: async (id: string): Promise<ApiResponse<void>> => {
    const res = await API.delete(`/needs/${id}`);
    return res.data;
  },
};

// Gemini AI API
export const aiApi = {
  extractDonation: async (text: string): Promise<ApiResponse<AiExtractionResponse>> => {
    const res = await API.post('/ai/extract-donation', { text });
    return res.data;
  },
  screenSafety: async (donation: Partial<Donation>): Promise<ApiResponse<any>> => {
    const res = await API.post('/ai/screen-safety', donation);
    return res.data;
  },
  getAnalyticsReport: async (): Promise<ApiResponse<AiAnalyticsReport>> => {
    const res = await API.get('/ai/analytics-report');
    return res.data;
  },
  chat: async (message: string): Promise<ApiResponse<AiChatResponse>> => {
    const res = await API.post('/ai/chat', { message });
    return res.data;
  },
  publicChat: async (message: string): Promise<ApiResponse<AiChatResponse>> => {
    const res = await API.post('/ai/public/chat', { message });
    return res.data;
  },
  getChatHistory: async (): Promise<ApiResponse<any[]>> => {
    const res = await API.get('/ai/chat-history');
    return res.data;
  },
};

// Notifications API
export const notificationApi = {
  getAll: async (): Promise<ApiResponse<NotificationItem[]>> => {
    const res = await API.get('/notifications');
    return res.data;
  },
  getUnreadCount: async (): Promise<ApiResponse<{ unreadCount: number }>> => {
    const res = await API.get('/notifications/unread-count');
    return res.data;
  },
  markRead: async (id: string): Promise<ApiResponse<NotificationItem>> => {
    const res = await API.put(`/notifications/${id}/read`);
    return res.data;
  },
  markAllRead: async (): Promise<ApiResponse<void>> => {
    const res = await API.put('/notifications/read-all');
    return res.data;
  },
};

// Dashboards API
export const dashboardApi = {
  getDonor: async (): Promise<ApiResponse<DonorDashboard>> => {
    const res = await API.get('/donor/dashboard');
    return res.data;
  },
  getReceiver: async (lat?: number, lon?: number): Promise<ApiResponse<ReceiverDashboard>> => {
    const res = await API.get('/receiver/dashboard', { params: { lat, lon } });
    return res.data;
  },
  getAdmin: async (): Promise<ApiResponse<AdminDashboard>> => {
    const res = await API.get('/admin/dashboard');
    return res.data;
  },
};

// Impact API
export const impactApi = {
  getSummary: async (): Promise<ApiResponse<ImpactSummary>> => {
    const res = await API.get('/impact/summary');
    return res.data;
  },
  getPublicSummary: async (): Promise<ApiResponse<ImpactSummary>> => {
    const res = await API.get('/impact/public/summary');
    return res.data;
  },
};

// Admin API
export const adminApi = {
  getUsers: async (): Promise<ApiResponse<User[]>> => {
    const res = await API.get('/admin/users');
    return res.data;
  },
  getAllDonations: async (): Promise<ApiResponse<Donation[]>> => {
    const res = await API.get('/admin/donations');
    return res.data;
  },
  verifyOrg: async (id: string, approve: boolean, notes?: string): Promise<ApiResponse<User>> => {
    const res = await API.put(`/admin/users/${id}/verify`, { approve, notes });
    return res.data;
  },
  toggleActive: async (id: string, active: boolean): Promise<ApiResponse<User>> => {
    const res = await API.put(`/admin/users/${id}/toggle-active`, { active });
    return res.data;
  },
  moderateDonation: async (id: string, status: DonationStatus, reason?: string): Promise<ApiResponse<void>> => {
    const res = await API.put(`/admin/donations/${id}/moderate`, { status, reason });
    return res.data;
  },
  getAuditLogs: async (): Promise<ApiResponse<any[]>> => {
    const res = await API.get('/admin/audit-logs');
    return res.data;
  },
};

// Data Seed API
export const seedApi = {
  seed: async (): Promise<ApiResponse<string>> => {
    const res = await API.post('/public/seed');
    return res.data;
  },
};

export default API;
