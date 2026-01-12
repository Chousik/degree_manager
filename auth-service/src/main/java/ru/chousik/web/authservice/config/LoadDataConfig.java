package ru.chousik.web.authservice.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.entity.UserProfileEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.UserProfileRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.time.OffsetDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class LoadDataConfig {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthoritiesRepository authoritiesRepository;
    UserProfileRepository userProfileRepository;
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

        if (userProfileRepository.findByUsername("chousik").isEmpty()) {
            UserProfileEntity profile = new UserProfileEntity();
            profile.setUsername("chousik");
            profile.setEmail("chousik@fixly.local");
            profile.setName("Admin");
            profile.setSurname("Fixly");
            profile.setCity("Москва");
            profile.setStatus("ACTIVE");
            profile.setCreatedAt(OffsetDateTime.now());
            userProfileRepository.save(profile);
        }
    }
}
