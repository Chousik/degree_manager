package ru.chousik.web.authservice.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.Collection;
import java.util.List;

@JsonTypeName("ru.chousik.web.authservice.security.DegreeUserDetails")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(value = { "authorities", "authoritiesEntities" }, ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
