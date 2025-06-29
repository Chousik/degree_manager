package ru.chousik.web.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.user_service.entity.TeacherEntity;

import java.util.Optional;
import java.util.UUID;

//#todo пиздец, для тестов пойдет
@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, UUID> {
    Optional<TeacherEntity> getTeacherEntityByNameAndSurnameAndMiddleName(String name,
                                                                          String surname,
                                                                          String middleName);
}
