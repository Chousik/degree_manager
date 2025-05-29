package ru.chousik.web.authservice.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.web.authservice.entity.TeacherEntity;
import ru.chousik.web.authservice.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>{
    Optional<UserEntity> getUserEntitiesByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByTeacher(TeacherEntity teacher);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.password = :password WHERE u.username = :username")
    void updatePasswordByUsername(@Param("password") @NotNull String password,
                                  @Param("username") String username);
    @Transactional
    void deleteByUsername(String username);
}
