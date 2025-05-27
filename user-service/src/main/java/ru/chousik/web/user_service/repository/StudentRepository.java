package ru.chousik.web.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.user_service.entity.StudentEntity;

import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity,
        UUID> {
    StudentEntity getStudentEntityByUuid(UUID uuid);
}
