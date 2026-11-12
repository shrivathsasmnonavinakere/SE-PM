package com.annadhata.dto;

public class RescueRelayActionRequest {
    private Integer extendDeadlineMinutes;
    private String notes;

    public RescueRelayActionRequest() {}

    public Integer getExtendDeadlineMinutes() { return extendDeadlineMinutes; }
    public void setExtendDeadlineMinutes(Integer extendDeadlineMinutes) { this.extendDeadlineMinutes = extendDeadlineMinutes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

