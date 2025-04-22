package ru.chousik.web.authservice.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.dto.AdminChangePasswordDto;
import ru.chousik.web.authservice.dto.ChangePasswordDto;
import ru.chousik.web.authservice.dto.RegisterUserDto;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountService {
    UserDetailsManager userDetailsManager;
    PasswordEncoder passwordEncoder;

    public void register(RegisterUserDto dto){
        if (userDetailsManager.userExists(dto.getUsername())){
            throw new IllegalArgumentException("Username already exists");
        }
        UserDetails user = User.withUsername(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles("USER")
                .build();
        userDetailsManager.createUser(user);
    }

    public void changeOwnPassword(String username, ChangePasswordDto dto){
        UserDetails user = userDetailsManager.loadUserByUsername(username);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Old password is incorrect");
        }
        UserDetails update = User.withUsername(username)
                .password(passwordEncoder.encode(dto.getNewPassword()))
                .build();
        userDetailsManager.updateUser(update);
    }
    public void changeUserPassword(String username, AdminChangePasswordDto dto){
        if (!userDetailsManager.userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        UserDetails user = userDetailsManager.loadUserByUsername(username);
        UserDetails update = User.withUsername(username)
                .password(passwordEncoder.encode(dto.getPassword()))
                .authorities(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new))
                .build();
        userDetailsManager.updateUser(update);
    }

    public void addAdminRole(String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new IllegalArgumentException("User not found");
        }
        UserDetails user = userDetailsManager.loadUserByUsername(username);
        Set<GrantedAuthority> updatedAuth = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        updatedAuth.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserDetails update = User.withUsername(username)
                .password(user.getPassword())
                .authorities(updatedAuth)
                .build();
        userDetailsManager.updateUser(update);
    }
}
