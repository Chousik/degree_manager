package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>{

}
