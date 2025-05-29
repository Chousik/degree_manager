package ru.chousik.web.authservice.services;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.TeacherEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.TeacherRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    AuthoritiesRepository authoritiesRepository;
    UserRepository userRepository;
    TeacherRepository teacherRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterUserDTO dto){
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
                teacher);
        user = userRepository.save(user);

        authoritiesRepository.save(new AuthoritiesEntity(user,
                "ROLE_USER"));
    }

    @Override
    public void deleteUser(String username){
        if (!userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Username does not exist");
        }
        userRepository.deleteByUsername(username);
    }

    @Override
    public void changeOwnPassword(String username, ChangePasswordDTO dto){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User don't exist"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Old password is incorrect");
        }

        userRepository.updatePasswordByUsername(passwordEncoder.encode(dto.getNewPassword())
                , username);
    }

    @Override
    public void changeUserPassword(String username, AdminChangePasswordDTO dto){
        if (!userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("User not found");
        }
        userRepository.updatePasswordByUsername(passwordEncoder.encode(dto.getPassword()),
                username);
    }

    @Override
    public void addAdminRole(String username) {
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_ADMIN"));
    }

    @Override
    public void removeAdminRole(String username){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (authoritiesRepository.getAuthoritiesEntityByUser(user).
                stream()
                .filter(r -> r.getAuthority().equals("ROLE_ADMIN"))
                .toList()
                .isEmpty()){
            throw new IllegalArgumentException("User don't have admin role");
        }
        authoritiesRepository.removeByUser(user);
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private UserDTO mapToDTO(UserEntity user){
        List<String> authorities = authoritiesRepository.getAuthoritiesEntityByUser(user)
                .stream()
                .map(Objects::toString)
                .toList();
        return new UserDTO(user.getUsername(),
                authorities,
                user.getTeacher().getName(),
                user.getTeacher().getSurname(),
                user.getTeacher().getMiddleName(),
                user.getTeacher().getAcademicStatus());
    }
}
