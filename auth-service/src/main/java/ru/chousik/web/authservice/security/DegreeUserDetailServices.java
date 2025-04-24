package ru.chousik.web.authservice.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DegreeUserDetailServices implements UserDetailsService {
    AuthoritiesRepository authoritiesRepository;
    UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        List<AuthoritiesEntity> authoritiesEntities = authoritiesRepository
                .getAuthoritiesEntityByUser(user);
        return new DegreeUserDetails(user, authoritiesEntities);
    }
}
