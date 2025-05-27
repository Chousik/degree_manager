package ru.chousik.web.taskservice.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolsConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
