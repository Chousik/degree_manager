package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.authservice.entity.UserProfileEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserProfileEntity> findByEmail(String email);
    Optional<UserProfileEntity> findByUsername(String username);
}
