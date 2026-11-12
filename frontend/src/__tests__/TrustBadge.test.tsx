import '@testing-library/jest-dom/vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TrustBadge } from '../components/TrustBadge';
import { trustApi } from '../services/api';

vi.mock('../services/api', () => ({
  trustApi: {
    getByUser: vi.fn(),
  },
}));

describe('TrustBadge component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders trust score and tier badge', () => {
    render(<TrustBadge score={92.4} tier="PLATINUM" label="Spice Garden" />);
    expect(screen.getByText(/Spice Garden: 92\/100 • PLATINUM/i)).toBeInTheDocument();
  });

  it('loads and displays transparent breakdown modal when clicked', async () => {
    vi.mocked(trustApi.getByUser).mockResolvedValue({
      success: true,
      message: 'OK',
      data: {
        userId: 'user-1',
        userName: 'Hope Care Shelter',
        role: 'ROLE_RECEIVER',
        overallScore: 88.5,
        tier: 'GOLD',
        factors: [
          {
            factorName: 'On-Time Pickup Adherence',
            weightPercent: 40,
            score: 90,
            detail: '9 of 10 pickups arrived before grace window.',
          },
          {
            factorName: 'Redistribution Completion Rate',
            weightPercent: 35,
            score: 85,
            detail: '85% completion across historical claims.',
          },
        ],
        completedCount: 17,
        totalTransactions: 20,
        onTimePickups: 9,
        missedPickups: 1,
        cancelledCount: 0,
        safetyFlagsCount: 0,
        explanation: 'Consistent punctual NGO partner.',
        calculatedAt: new Date().toISOString(),
      },
    });

    const user = userEvent.setup();
    render(<TrustBadge score={88.5} tier="GOLD" userId="user-1" compact />);

    await user.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(trustApi.getByUser).toHaveBeenCalledWith('user-1');
    });

    expect(await screen.findByText('Hope Care Shelter')).toBeInTheDocument();
    expect(screen.getByText(/On-Time Pickup Adherence/i)).toBeInTheDocument();
    expect(screen.getByText('Consistent punctual NGO partner.')).toBeInTheDocument();
  });
});

