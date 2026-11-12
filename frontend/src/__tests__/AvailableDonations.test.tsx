import '@testing-library/jest-dom/vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AvailableDonationsPage } from '../pages/AvailableDonationsPage';
import { donationApi, claimApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../services/api', () => ({
  donationApi: {
    getAvailable: vi.fn(),
  },
  claimApi: {
    create: vi.fn(),
  },
  trustApi: {
    getByUser: vi.fn(),
  },
}));

vi.mock('../components/LeafletMap', () => ({
  LeafletMap: () => <div data-testid="mock-leaflet-map" />,
}));

const partialDonation = {
  id: 'don-partial-1',
  donorId: 'donor-1',
  donorName: 'Spice Garden Kitchen',
  foodName: 'Vegetable Biryani Tray',
  foodDescription: 'Fresh aromatic vegetable biryani',
  foodCategory: 'Cooked food' as const,
  quantity: 40,
  remainingQuantity: 25,
  allocatedQuantity: 15,
  allocationCount: 1,
  measurementUnit: 'kg',
  preparationDateTime: new Date().toISOString(),
  pickupDeadline: new Date(Date.now() + 4 * 3600 * 1000).toISOString(),
  pickupAddress: 'MG Road, Bangalore',
  status: 'PARTIALLY_CLAIMED' as const,
  safetyIndicator: 'LOW_RISK' as const,
  distanceKm: 2.4,
  donorTrustScore: 95,
  donorTrustTier: 'PLATINUM',
  createdAt: new Date().toISOString(),
};

describe('AvailableDonationsPage partial distribution and verification guard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(donationApi.getAvailable).mockResolvedValue({
      success: true,
      message: 'OK',
      data: [partialDonation],
    });
  });

  it('shows remaining quantity, donor trust badge, and enforces remaining max in claim modal', async () => {
    vi.mocked(useAuth).mockReturnValue({
      user: {
        id: 'rec-1',
        name: 'Hope Care NGO',
        email: 'hope@shelter.org',
        role: 'ROLE_RECEIVER',
        verificationStatus: 'VERIFIED',
        active: true,
        latitude: 12.9716,
        longitude: 77.5946,
      },
      token: 'tok',
      role: 'ROLE_RECEIVER',
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      refreshUser: vi.fn(),
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AvailableDonationsPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Vegetable Biryani Tray')).toBeInTheDocument();
    expect(screen.getByText(/25\.0 \/ 40 kg/i)).toBeInTheDocument();
    expect(screen.getByText(/Spice Garden Kitchen: 95/i)).toBeInTheDocument();

    const claimBtn = screen.getByRole('button', { name: /Claim Full or Partial Portion/i });
    expect(claimBtn).toBeEnabled();
    await user.click(claimBtn);

    const qtyInput = screen.getByRole('spinbutton') as HTMLInputElement;
    expect(qtyInput.value).toBe('25');
    expect(qtyInput.max).toBe('25');
  });

  it('disables claim action and shows warning banner for unverified receiver', async () => {
    vi.mocked(useAuth).mockReturnValue({
      user: {
        id: 'rec-pending',
        name: 'Pending Shelter',
        email: 'pending@ngo.org',
        role: 'ROLE_RECEIVER',
        verificationStatus: 'PENDING',
        active: true,
      },
      token: 'tok',
      role: 'ROLE_RECEIVER',
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      refreshUser: vi.fn(),
    });

    render(
      <MemoryRouter>
        <AvailableDonationsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Organization Verification Required Before Claiming/i)).toBeInTheDocument();
    });

    const claimBtn = await screen.findByRole('button', { name: /Verification Pending/i });
    expect(claimBtn).toBeDisabled();
  });
});
