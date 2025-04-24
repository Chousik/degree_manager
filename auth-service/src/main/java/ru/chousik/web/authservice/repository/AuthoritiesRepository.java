package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.List;

public interface AuthoritiesRepository extends JpaRepository<AuthoritiesEntity, String> {
    List<AuthoritiesEntity> getAuthoritiesEntityByUsername(UserEntity user);
}
