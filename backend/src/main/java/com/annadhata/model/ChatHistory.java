package com.annadhata.model;

import com.annadhata.enums.Role;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chatHistory")
public class ChatHistory {

    @Id
    private String id;

    @Indexed
    private String userId;

    private Role role;
    private String userMessage;
    private String assistantResponse;

    @CreatedDate
    @Indexed
    private Instant timestamp = Instant.now();

    public ChatHistory() {}

    public ChatHistory(String userId, Role role, String userMessage, String assistantResponse) {
        this.userId = userId;
        this.role = role;
        this.userMessage = userMessage;
        this.assistantResponse = assistantResponse;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public String getAssistantResponse() { return assistantResponse; }
    public void setAssistantResponse(String assistantResponse) { this.assistantResponse = assistantResponse; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
