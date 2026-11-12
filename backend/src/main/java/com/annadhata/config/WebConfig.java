package com.annadhata.config;

import com.annadhata.enums.FoodCategory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new Converter<String, FoodCategory>() {
            @Override
            public FoodCategory convert(@NonNull String source) {
                return FoodCategory.fromString(source);
            }
        });
    }
}

