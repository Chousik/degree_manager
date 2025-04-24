package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.authservice.entity.JwkEntity;

import java.util.Optional;

@Repository
public interface JwkRepository extends JpaRepository<JwkEntity, String> {
    Optional<JwkEntity> getJwkEntitiesById(String id);
}
