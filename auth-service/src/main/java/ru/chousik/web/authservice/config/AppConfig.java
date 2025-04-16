package ru.chousik.web.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import ru.chousik.web.authservice.repository.UserRepository;
import ru.chousik.web.authservice.security.AuthUserDetailsManager;

@Configuration
public class AppConfig {
    @Bean
    public UserDetailsManager userDetailsManager(UserRepository userRepository){
        return new AuthUserDetailsManager(userRepository);
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
