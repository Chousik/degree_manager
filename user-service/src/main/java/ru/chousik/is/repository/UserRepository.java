package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
