package ru.chousik.web.authservice.services;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.dto.AdminChangePasswordDto;
import ru.chousik.web.authservice.dto.ChangePasswordDto;
import ru.chousik.web.authservice.dto.RegisterUserDto;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.TeacherEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.TeacherRepository;
import ru.chousik.web.authservice.repository.UserRepository;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    AuthoritiesRepository authoritiesRepository;
    UserRepository userRepository;
    TeacherRepository teacherRepository;
    PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterUserDto dto){
        if (userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("Username already exists");
        }
        TeacherEntity teacher = teacherRepository.
                getTeacherEntityByNameAndSurnameAndMiddleName(dto.getName(),
                        dto.getSurname(), dto.getMiddleName())
                .orElseThrow(() -> new IllegalArgumentException("Teacher don't exist"));
        if (userRepository.existsByTeacher(teacher)){
            throw new IllegalArgumentException("Teacher already exists");
        }

        UserEntity user = new UserEntity(dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                true,
                teacher.getId());
        userRepository.save(user);

        authoritiesRepository.save(new AuthoritiesEntity(user,
                "ROLE_USER"));
    }

    public void changeOwnPassword(String username, ChangePasswordDto dto){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User don't exist"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Old password is incorrect");
        }

        userRepository.updatePasswordByUsername(dto.getNewPassword(), username);
    }

    public void changeUserPassword(String username, AdminChangePasswordDto dto){
        if (!userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("User not found");
        }
        userRepository.updatePasswordByUsername(dto.getPassword(), username);
    }

    public void addAdminRole(String username) {
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_ADMIN"));
    }
}
