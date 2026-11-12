package com.annadhata.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FoodCategory {
    COOKED_FOOD("Cooked food"),
    RICE_AND_GRAINS("Rice and grains"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    BREAD_AND_BAKERY("Bread and bakery"),
    PACKAGED_FOOD("Packaged food"),
    DAIRY_PRODUCTS("Dairy products"),
    OTHER("Other");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static FoodCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim();
        for (FoodCategory c : FoodCategory.values()) {
            if (c.name().equalsIgnoreCase(clean) || 
                c.displayName.equalsIgnoreCase(clean) ||
                clean.toLowerCase().contains(c.name().toLowerCase().replace("_", " "))) {
                return c;
            }
        }
        return OTHER;
    }
}
