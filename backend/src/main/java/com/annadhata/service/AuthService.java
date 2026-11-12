package com.annadhata.service;

import com.annadhata.dto.AuthRequest;
import com.annadhata.dto.AuthResponse;
import com.annadhata.dto.RegisterRequest;
import com.annadhata.dto.UpdateProfileRequest;
import com.annadhata.dto.UserProfileResponse;
import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.DuplicateResourceException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.User;
import com.annadhata.model.VerificationRequest;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VerificationRequestRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import com.annadhata.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       VerificationRequestRepository verificationRequestRepository,
                       VolunteerProfileRepository volunteerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ROLE_ADMIN) {
            throw new ForbiddenException("Administrator accounts cannot be self-registered.");
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setOrganizationName(request.getOrganizationName() != null ? request.getOrganizationName() : request.getName());
        user.setOrganizationType(request.getOrganizationType());
        user.setRegistrationNumber(request.getRegistrationNumber());
        user.setLatitude(request.getLatitude() != null ? request.getLatitude() : 12.9716);
        user.setLongitude(request.getLongitude() != null ? request.getLongitude() : 77.5946);
        if (request.getVerificationDocuments() != null) {
            user.setVerificationDocuments(new ArrayList<>(request.getVerificationDocuments()));
        }
        user.setActive(true);

        // Receivers (NGOs) require admin document review before claiming food
        if (request.getRole() == Role.ROLE_RECEIVER) {
            user.setVerificationStatus(VerificationStatus.PENDING);
        } else {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
        }

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.ROLE_RECEIVER) {
            VerificationRequest verificationRequest = new VerificationRequest();
            verificationRequest.setUserId(savedUser.getId());
            verificationRequest.setUserEmail(savedUser.getEmail());
            verificationRequest.setOrganizationName(savedUser.getOrganizationName());
            verificationRequest.setOrganizationType(savedUser.getOrganizationType());
            verificationRequest.setRegistrationNumber(savedUser.getRegistrationNumber());
            verificationRequest.setDocumentUrls(savedUser.getVerificationDocuments() != null
                    ? savedUser.getVerificationDocuments()
                    : new ArrayList<>());
            verificationRequest.setStatus(VerificationStatus.PENDING);
            verificationRequestRepository.save(verificationRequest);
        } else if (savedUser.getRole() == Role.ROLE_VOLUNTEER) {
            VolunteerProfile vp = new VolunteerProfile();
            vp.setUserId(savedUser.getId());
            vp.setUserName(savedUser.getName());
            vp.setPhone(savedUser.getPhone());
            vp.setLatitude(savedUser.getLatitude());
            vp.setLongitude(savedUser.getLongitude());
            vp.setAddress(savedUser.getAddress());
            vp.setVehicleType(request.getVehicleType() != null ? request.getVehicleType() : "Two-Wheeler / Scooter");
            vp.setMaxCapacityKg(request.getMaxCapacityKg() != null ? request.getMaxCapacityKg() : 25.0);
            vp.setServiceRadiusKm(request.getServiceRadiusKm() != null ? request.getServiceRadiusKm() : 15.0);
            vp.setAvailableForDispatch(true);
            volunteerProfileRepository.save(vp);
        }

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());

        return new AuthResponse(
                token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole(),
                savedUser.getOrganizationName(), savedUser.getOrganizationType(), savedUser.getVerificationStatus(),
                savedUser.getLatitude(), savedUser.getLongitude(), savedUser.getAddress()
        );
    }

    public AuthResponse login(AuthRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + cleanEmail));

        if (!user.isActive()) {
            throw new BadRequestException("Your account has been deactivated. Please contact administrator.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(
                token, user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getOrganizationName(), user.getOrganizationType(), user.getVerificationStatus(),
                user.getLatitude(), user.getLongitude(), user.getAddress()
        );
    }

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return mapToProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getOrganizationName() != null) user.setOrganizationName(req.getOrganizationName());
        if (req.getOrganizationType() != null) user.setOrganizationType(req.getOrganizationType());
        if (req.getRegistrationNumber() != null) user.setRegistrationNumber(req.getRegistrationNumber());
        if (req.getLatitude() != null) user.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) user.setLongitude(req.getLongitude());

        boolean docsUpdated = false;
        if (req.getVerificationDocuments() != null) {
            user.setVerificationDocuments(req.getVerificationDocuments());
            docsUpdated = true;
        }

        // Allow rejected organisations to re-submit updated docs/registration info for Admin review
        if (user.getRole() == Role.ROLE_RECEIVER
                && user.getVerificationStatus() == VerificationStatus.REJECTED
                && (docsUpdated || req.getRegistrationNumber() != null)) {
            user.setVerificationStatus(VerificationStatus.PENDING);
        }

        User updated = userRepository.save(user);

        if (updated.getRole() == Role.ROLE_RECEIVER && (docsUpdated || req.getRegistrationNumber() != null)) {
            VerificationRequest verReq = verificationRequestRepository.findByUserId(updated.getId())
                    .orElse(new VerificationRequest());
            verReq.setUserId(updated.getId());
            verReq.setUserEmail(updated.getEmail());
            verReq.setOrganizationName(updated.getOrganizationName());
            verReq.setOrganizationType(updated.getOrganizationType());
            verReq.setRegistrationNumber(updated.getRegistrationNumber());
            verReq.setDocumentUrls(updated.getVerificationDocuments());
            verReq.setStatus(updated.getVerificationStatus());
            verificationRequestRepository.save(verReq);
        }

        return mapToProfileResponse(updated);
    }

    public UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setPhone(user.getPhone());
        res.setAddress(user.getAddress());
        res.setOrganizationName(user.getOrganizationName());
        res.setOrganizationType(user.getOrganizationType());
        res.setRegistrationNumber(user.getRegistrationNumber());
        res.setVerificationStatus(user.getVerificationStatus());
        res.setVerificationDocuments(user.getVerificationDocuments());
        res.setLatitude(user.getLatitude());
        res.setLongitude(user.getLongitude());
        res.setTrustScore(user.getTrustScore());
        res.setTrustTier(user.getTrustTier());
        res.setActive(user.isActive());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
