package com.annadhata.dto;

import jakarta.validation.constraints.NotBlank;

public class AiExtractionRequest {

    @NotBlank(message = "Prompt text is required")
    private String text;

    public AiExtractionRequest() {}

    public AiExtractionRequest(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
