import '@testing-library/jest-dom/vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QrPassportModal } from '../components/QrPassportModal';
import { passportApi } from '../services/api';

vi.mock('../services/api', () => ({
  passportApi: {
    getByClaim: vi.fn(),
    verify: vi.fn(),
  },
}));

const samplePassport = {
  id: 'pass-1',
  claimId: 'claim-1',
  donationId: 'don-1',
  donorId: 'donor-1',
  receiverId: 'rec-1',
  donorName: 'Spice Garden Restaurant',
  receiverName: 'Hope Care Shelter',
  foodName: 'Paneer Pulao Tray',
  allocatedQuantity: 15,
  measurementUnit: 'kg',
  passportCode: 'AD-QR-7F9A12',
  hmacSignature: 'deadbeef123456',
  qrPayloadJson: '{"code":"AD-QR-7F9A12"}',
  qrImageBase64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA',
  status: 'ACTIVE' as const,
  safetyIndicator: 'LOW_RISK' as const,
  issuedAt: new Date().toISOString(),
  expiresAt: new Date(Date.now() + 2 * 3600 * 1000).toISOString(),
};

describe('QrPassportModal component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders QR image, passport code, and triggers verify callback', async () => {
    vi.mocked(passportApi.getByClaim).mockResolvedValue({
      success: true,
      message: 'OK',
      data: samplePassport,
    });
    vi.mocked(passportApi.verify).mockResolvedValue({
      success: true,
      message: 'Verified',
      data: {
        ...samplePassport,
        status: 'VERIFIED_PICKUP',
        verifiedByName: 'Spice Garden Staff',
        verifiedAt: new Date().toISOString(),
      },
    });

    const onVerified = vi.fn();
    const user = userEvent.setup();

    render(
      <QrPassportModal
        claimId="claim-1"
        canVerify={true}
        onVerified={onVerified}
        onClose={vi.fn()}
      />
    );

    expect(await screen.findByText('AD-QR-7F9A12')).toBeInTheDocument();
    const img = screen.getByAltText('QR Code for AD-QR-7F9A12') as HTMLImageElement;
    expect(img).toHaveAttribute('src', samplePassport.qrImageBase64);

    const verifyButton = screen.getByRole('button', { name: /Verify Handover & Mark Picked Up/i });
    await user.click(verifyButton);

    await waitFor(() => {
      expect(passportApi.verify).toHaveBeenCalledWith('AD-QR-7F9A12', 'Verified via QR Food Passport modal');
      expect(onVerified).toHaveBeenCalled();
    });

    expect(await screen.findByText(/Spice Garden Staff/i)).toBeInTheDocument();
  });
});

