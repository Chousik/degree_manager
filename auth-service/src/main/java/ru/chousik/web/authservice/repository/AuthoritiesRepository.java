package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.List;

@Repository
public interface AuthoritiesRepository extends JpaRepository<AuthoritiesEntity, String> {
    List<AuthoritiesEntity> getAuthoritiesEntityByUser(UserEntity user);
}
