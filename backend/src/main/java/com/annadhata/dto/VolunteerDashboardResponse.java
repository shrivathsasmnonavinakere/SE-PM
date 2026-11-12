package com.annadhata.dto;

import com.annadhata.model.VolunteerProfile;

import java.util.List;

public class VolunteerDashboardResponse {
    private long openTasksCount;
    private long activeMissionsCount;
    private int completedDeliveriesCount;
    private double totalKgTransported;
    private long estimatedMealsDelivered;
    private double trustScore;
    private String trustTier;
    private VolunteerProfile volunteerProfile;
    private List<VolunteerTaskResponse> activeMissions;
    private List<VolunteerTaskResponse> openTasks;

    public VolunteerDashboardResponse() {}

    public long getOpenTasksCount() { return openTasksCount; }
    public void setOpenTasksCount(long openTasksCount) { this.openTasksCount = openTasksCount; }

    public long getActiveMissionsCount() { return activeMissionsCount; }
    public void setActiveMissionsCount(long activeMissionsCount) { this.activeMissionsCount = activeMissionsCount; }

    public int getCompletedDeliveriesCount() { return completedDeliveriesCount; }
    public void setCompletedDeliveriesCount(int completedDeliveriesCount) { this.completedDeliveriesCount = completedDeliveriesCount; }

    public double getTotalKgTransported() { return totalKgTransported; }
    public void setTotalKgTransported(double totalKgTransported) { this.totalKgTransported = totalKgTransported; }

    public long getEstimatedMealsDelivered() { return estimatedMealsDelivered; }
    public void setEstimatedMealsDelivered(long estimatedMealsDelivered) { this.estimatedMealsDelivered = estimatedMealsDelivered; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public VolunteerProfile getVolunteerProfile() { return volunteerProfile; }
    public void setVolunteerProfile(VolunteerProfile volunteerProfile) { this.volunteerProfile = volunteerProfile; }

    public List<VolunteerTaskResponse> getActiveMissions() { return activeMissions; }
    public void setActiveMissions(List<VolunteerTaskResponse> activeMissions) { this.activeMissions = activeMissions; }

    public List<VolunteerTaskResponse> getOpenTasks() { return openTasks; }
    public void setOpenTasks(List<VolunteerTaskResponse> openTasks) { this.openTasks = openTasks; }
}

