package ru.chousik.web.authservice.config;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.TeacherEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.TeacherRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class LoadDataConfig {
    TeacherRepository teacherRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthoritiesRepository authoritiesRepository;
    @PostConstruct
    public void initial(){
        if (userRepository.getUserEntitiesByUsername("chousik").isEmpty()){
            TeacherEntity teacher = new TeacherEntity(
                    "Захар",
                    "Силаев",
                    "Алексеевич",
                    "магистр йода");
            teacherRepository.save(teacher);

            UserEntity user = new UserEntity("chousik", passwordEncoder.encode("chousik"),
                    true, teacher);
            userRepository.save(user);

            authoritiesRepository.save(new AuthoritiesEntity(user,
                    "ROLE_ADMIN"));
        }
        Optional<TeacherEntity> optionalTeacher = teacherRepository.getTeacherEntityByNameAndSurnameAndMiddleName(
                "Елена", "Путинцева", "Валентиновна");

        if (userRepository.getUserEntitiesByUsername("hipeoplea").isEmpty() && optionalTeacher.isPresent()) {
            TeacherEntity teacher = optionalTeacher.get();
            UserEntity user = new UserEntity(
                    "hipeoplea",
                    passwordEncoder.encode("hipeoplea"),
                    true,
                    teacher
            );

            userRepository.save(user);
            authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_ADMIN"));
        }
    }
}