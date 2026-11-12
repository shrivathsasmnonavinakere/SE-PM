import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AiChatWidget } from './components/AiChatWidget';

// Public Pages
import { LandingPage } from './pages/LandingPage';
import { AboutPage } from './pages/AboutPage';
import { HowItWorksPage } from './pages/HowItWorksPage';
import { FeaturesPage } from './pages/FeaturesPage';
import { ImpactDashboardPage } from './pages/ImpactDashboardPage';
import { ContactPage } from './pages/ContactPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ProfilePage } from './pages/ProfilePage';

// Donor Pages
import { DonorDashboardPage } from './pages/DonorDashboard';
import { CreateDonationPage } from './pages/CreateDonationPage';
import { MyDonationsPage } from './pages/MyDonationsPage';
import { DonationDetailsPage } from './pages/DonationDetailsPage';
import { MatchingRecommendationsPage } from './pages/MatchingRecommendationsPage';
import { DonorClaimsPage } from './pages/DonorClaimsPage';

// Receiver Pages
import { ReceiverDashboardPage } from './pages/ReceiverDashboard';
import { AvailableDonationsPage } from './pages/AvailableDonationsPage';
import { DonationMapPage } from './pages/DonationMapPage';
import { ReceiverClaimsPage } from './pages/ReceiverClaimsPage';
import { FoodRequirementsPage } from './pages/FoodRequirementsPage';

// Admin & Shared Operational Pages
import { AdminDashboardPage } from './pages/AdminDashboard';
import { UserManagementPage } from './pages/UserManagementPage';
import { OrganisationVerificationPage } from './pages/OrganisationVerificationPage';
import { DonationModerationPage } from './pages/DonationModerationPage';
import { AiAnalyticsPage } from './pages/AiAnalyticsPage';
import { DemandHeatmapPage } from './pages/DemandHeatmapPage';
import { VolunteerDashboardPage } from './pages/VolunteerDashboardPage';
import { PassportVerifyPage } from './pages/PassportVerifyPage';
import { RescueRelayPage } from './pages/RescueRelayPage';

export const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 selection:bg-emerald-500 selection:text-white">
          <Navbar />
          <main className="flex-1">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/about" element={<AboutPage />} />
              <Route path="/how-it-works" element={<HowItWorksPage />} />
              <Route path="/features" element={<FeaturesPage />} />
              <Route path="/impact" element={<ImpactDashboardPage />} />
              <Route path="/contact" element={<ContactPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Shared Authenticated */}
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <ProfilePage />
                  </ProtectedRoute>
                }
              />

              {/* Donor Routes */}
              <Route
                path="/donor/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <DonorDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/donor/create-donation"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <CreateDonationPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/donor/my-donations"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <MyDonationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/donor/donations/:id"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <DonationDetailsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/donor/donations/:id/matches"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <MatchingRecommendationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/donor/claims"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_ADMIN']}>
                    <DonorClaimsPage />
                  </ProtectedRoute>
                }
              />

              {/* Receiver Routes */}
              <Route
                path="/receiver/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <ReceiverDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/receiver/donations"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <AvailableDonationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/receiver/map"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <DonationMapPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/receiver/claims"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <ReceiverClaimsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/receiver/requirements"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <FoodRequirementsPage />
                  </ProtectedRoute>
                }
              />

              {/* Admin Routes */}
              <Route
                path="/admin/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/verifications"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                    <OrganisationVerificationPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/moderation"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                    <DonationModerationPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/analytics"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                    <AiAnalyticsPage />
                  </ProtectedRoute>
                }
              />

              {/* Volunteer Route */}
              <Route
                path="/volunteer/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_VOLUNTEER']}>
                    <VolunteerDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Shared Operational & Intelligence Routes */}
              <Route
                path="/heatmap"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_RECEIVER', 'ROLE_VOLUNTEER', 'ROLE_ADMIN']}>
                    <DemandHeatmapPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/verify-passport"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_VOLUNTEER', 'ROLE_ADMIN']}>
                    <PassportVerifyPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/rescue-relay"
                element={
                  <ProtectedRoute allowedRoles={['ROLE_DONOR', 'ROLE_RECEIVER', 'ROLE_ADMIN']}>
                    <RescueRelayPage />
                  </ProtectedRoute>
                }
              />

              {/* Catch-all redirect */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <Footer />
          <AiChatWidget />
        </div>
      </AuthProvider>
    </Router>
  );
};

export default App;
