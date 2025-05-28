package ru.chousik.web.user_service.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ToolsConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
