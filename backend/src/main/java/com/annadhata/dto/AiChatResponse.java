package com.annadhata.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {
    private String reply;
    private List<String> quickSuggestions = new ArrayList<>();
    private List<String> relevantActions = new ArrayList<>();
    private Instant timestamp = Instant.now();

    public AiChatResponse() {}

    public AiChatResponse(String reply, List<String> quickSuggestions) {
        this.reply = reply;
        this.quickSuggestions = quickSuggestions;
        this.timestamp = Instant.now();
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public List<String> getQuickSuggestions() { return quickSuggestions; }
    public void setQuickSuggestions(List<String> quickSuggestions) { this.quickSuggestions = quickSuggestions; }

    public List<String> getRelevantActions() { return relevantActions; }
    public void setRelevantActions(List<String> relevantActions) { this.relevantActions = relevantActions; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
