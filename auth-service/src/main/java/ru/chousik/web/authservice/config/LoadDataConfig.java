package ru.chousik.web.authservice.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.UserRepository;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class LoadDataConfig {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthoritiesRepository authoritiesRepository;
    @PostConstruct
    public void initial(){
        if (userRepository.getUserEntitiesByUsername("chousik").isEmpty()){
            UserEntity user = new UserEntity("chousik", passwordEncoder.encode("chousik"),
                    true);
            userRepository.save(user);

            authoritiesRepository.save(new AuthoritiesEntity(user,
                    "ROLE_USER"));
            authoritiesRepository.save(new AuthoritiesEntity(user,
                    "ROLE_ADMIN"));
        }
    }
}