package com.annadhata.service;

import com.annadhata.dto.CreateNeedRequest;
import com.annadhata.dto.NeedResponse;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.model.User;
import com.annadhata.repository.ReceiverNeedRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceiverNeedsService {

    private final ReceiverNeedRepository receiverNeedRepository;

    public ReceiverNeedsService(ReceiverNeedRepository receiverNeedRepository) {
        this.receiverNeedRepository = receiverNeedRepository;
    }

    public NeedResponse createNeed(CreateNeedRequest req, User receiver) {
        ReceiverNeed need = new ReceiverNeed();
        need.setReceiverId(receiver.getId());
        need.setReceiverName(receiver.getOrganizationName() != null ? receiver.getOrganizationName() : receiver.getName());
        need.setRequiredCategory(req.getRequiredCategory());
        need.setRequiredQuantity(req.getRequiredQuantity());
        need.setMeasurementUnit(req.getMeasurementUnit() != null ? req.getMeasurementUnit() : "kg");
        need.setPreferredFoodType(req.getPreferredFoodType());
        need.setNumberOfBeneficiaries(req.getNumberOfBeneficiaries() != null ? req.getNumberOfBeneficiaries() : 50);
        need.setMaxReceivingCapacity(req.getMaxReceivingCapacity() != null ? req.getMaxReceivingCapacity() : req.getRequiredQuantity() * 1.5);
        need.setRequiredDate(req.getRequiredDate());
        need.setPickupAvailable(req.isPickupAvailable());
        need.setPickupWindowStart(req.getPickupWindowStart() != null ? req.getPickupWindowStart() : "09:00");
        need.setPickupWindowEnd(req.getPickupWindowEnd() != null ? req.getPickupWindowEnd() : "21:00");
        need.setLatitude(req.getLatitude() != null ? req.getLatitude() : receiver.getLatitude());
        need.setLongitude(req.getLongitude() != null ? req.getLongitude() : receiver.getLongitude());
        need.setAddress(req.getAddress() != null ? req.getAddress() : receiver.getAddress());
        need.setActive(true);

        ReceiverNeed saved = receiverNeedRepository.save(need);
        return mapToResponse(saved);
    }

    public List<NeedResponse> getReceiverNeeds(String receiverId) {
        return receiverNeedRepository.findByReceiverIdAndActiveTrue(receiverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NeedResponse> getAllActiveNeeds() {
        return receiverNeedRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deactivateNeed(String needId, User receiver) {
        ReceiverNeed need = receiverNeedRepository.findById(needId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + needId));

        if (!need.getReceiverId().equals(receiver.getId()) && receiver.getRole() != com.annadhata.enums.Role.ROLE_ADMIN) {
            throw new ForbiddenException("Not authorized to modify this requirement");
        }

        need.setActive(false);
        receiverNeedRepository.save(need);
    }

    public NeedResponse mapToResponse(ReceiverNeed need) {
        NeedResponse resp = new NeedResponse();
        resp.setId(need.getId());
        resp.setReceiverId(need.getReceiverId());
        resp.setReceiverName(need.getReceiverName());
        resp.setRequiredCategory(need.getRequiredCategory());
        resp.setRequiredQuantity(need.getRequiredQuantity());
        resp.setMeasurementUnit(need.getMeasurementUnit());
        resp.setPreferredFoodType(need.getPreferredFoodType());
        resp.setNumberOfBeneficiaries(need.getNumberOfBeneficiaries());
        resp.setMaxReceivingCapacity(need.getMaxReceivingCapacity());
        resp.setRequiredDate(need.getRequiredDate());
        resp.setPickupAvailable(need.isPickupAvailable());
        resp.setPickupWindowStart(need.getPickupWindowStart());
        resp.setPickupWindowEnd(need.getPickupWindowEnd());
        resp.setLatitude(need.getLatitude());
        resp.setLongitude(need.getLongitude());
        resp.setAddress(need.getAddress());
        resp.setActive(need.isActive());
        resp.setCreatedAt(need.getCreatedAt());
        resp.setUpdatedAt(need.getUpdatedAt());
        return resp;
    }
}
