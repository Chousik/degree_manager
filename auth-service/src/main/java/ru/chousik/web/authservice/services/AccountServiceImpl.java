package ru.chousik.web.authservice.services;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.TeacherEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.exception.*;
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
            throw new UsernameExistsException(dto.getUsername());
        }

        String teacherData = String.join(" ",
                List.of(dto.getName(),
                        dto.getMiddleName(), dto.getSurname()));

        TeacherEntity teacher = teacherRepository.
                getTeacherEntityByNameAndSurnameAndMiddleName(dto.getName(),
                        dto.getSurname(), dto.getMiddleName())
                .orElseThrow(() -> new TeacherNotFoundException(teacherData));

        if (userRepository.existsByTeacher(teacher)){
            throw new TeacherAlreadyLinkedException(teacherData);
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
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        authoritiesRepository.removeByUser(user);
        userRepository.delete(user);
    }

    @Override
    public void changeOwnPassword(String username, ChangePasswordDTO dto){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new IncorrectOldPasswordException();
        }

        changePassword(username,
                passwordEncoder.encode(dto.getNewPassword()));
    }

    @Override
    public void changeUserPassword(String username, AdminChangePasswordDTO dto){
        if (!userRepository.existsByUsername(username)){
            throw new UserNotFoundException(username);
        }

        changePassword(username,
                passwordEncoder.encode(dto.getPassword()));
    }

    private void changePassword(String username, String password){
        if (!(passwordEncoder.matches(userRepository.getPasswordByUsername(username),
                password))){
            throw new PasswordReuseException();
        }

        userRepository.updatePasswordByUsername(passwordEncoder.encode(password),
                username);
    }

    @Override
    public void addAdminRole(String username) {
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!authoritiesRepository.getAuthoritiesEntityByUser(user).
                stream()
                .filter(r -> r.getAuthority().equals("ROLE_ADMIN"))
                .toList()
                .isEmpty()){
            throw new AdminRoleAlreadyAssignedException(username);
        }

        authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_ADMIN"));
    }

    @Override
    public void removeAdminRole(String username){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (authoritiesRepository.getAuthoritiesEntityByUser(user).
                stream()
                .filter(r -> r.getAuthority().equals("ROLE_ADMIN"))
                .toList()
                .isEmpty()){
            throw new AdminRoleNotAssignedException(username);
        }

        authoritiesRepository.removeByAuthorityAndUser("ROLE_ADMIN", user);
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
