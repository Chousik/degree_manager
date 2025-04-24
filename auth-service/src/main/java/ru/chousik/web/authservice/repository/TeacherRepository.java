package ru.chousik.web.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.web.authservice.entity.TeacherEntity;

import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository extends JpaRepository<TeacherEntity, UUID> {
    Optional<TeacherEntity> getTeacherEntityByNameAndSurnameAndMiddleName(String name,
                                                                          String surname,
                                                                          String middleName);
}
