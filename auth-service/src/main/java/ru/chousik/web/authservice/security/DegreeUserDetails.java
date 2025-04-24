package ru.chousik.web.authservice.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.Collection;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DegreeUserDetails implements UserDetails {
    UserEntity user;
    List<AuthoritiesEntity> authoritiesEntities;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authoritiesEntities.stream().map(AuthoritiesEntity::getAuthority)
                .map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
