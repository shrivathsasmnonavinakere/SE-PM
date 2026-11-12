import '@testing-library/jest-dom/vitest';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { LoginPage } from '../pages/LoginPage';
import { useAuth } from '../context/AuthContext';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

describe('ProtectedRoute and Volunteer Login redirect', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('blocks unauthorized role and shows link back to role dashboard', () => {
    vi.mocked(useAuth).mockReturnValue({
      user: {
        id: 'vol-1',
        name: 'Arjun Volunteer',
        email: 'volunteer@annadhata.org',
        role: 'ROLE_VOLUNTEER',
        verificationStatus: 'VERIFIED',
        active: true,
      },
      token: 'tok',
      role: 'ROLE_VOLUNTEER',
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      refreshUser: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin/dashboard']}>
        <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
          <div>Admin Secret Panel</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.queryByText('Admin Secret Panel')).not.toBeInTheDocument();
    const backLink = screen.getByRole('link');
    expect(backLink).toHaveAttribute('href', '/volunteer/dashboard');
  });

  it('logs in via Volunteer quick demo button and navigates to /volunteer/dashboard', async () => {
    const loginMock = vi.fn().mockResolvedValue({
      token: 'jwt-vol',
      type: 'Bearer',
      id: 'vol-1',
      name: 'Arjun Volunteer',
      email: 'volunteer@annadhata.org',
      role: 'ROLE_VOLUNTEER',
      verificationStatus: 'VERIFIED',
    });

    vi.mocked(useAuth).mockReturnValue({
      user: null,
      token: null,
      role: null,
      isAuthenticated: false,
      isLoading: false,
      login: loginMock,
      register: vi.fn(),
      logout: vi.fn(),
      refreshUser: vi.fn(),
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/volunteer/dashboard" element={<div>Volunteer Portal Loaded</div>} />
        </Routes>
      </MemoryRouter>
    );

    const volButton = screen.getByRole('button', { name: /Volunteer/i });
    await user.click(volButton);

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith({
        email: 'volunteer@annadhata.org',
        password: 'volunteer123',
      });
    });
    expect(await screen.findByText('Volunteer Portal Loaded')).toBeInTheDocument();
  });
});
