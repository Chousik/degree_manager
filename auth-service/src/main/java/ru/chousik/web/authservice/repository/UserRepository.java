package ru.chousik.web.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.chousik.web.authservice.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
