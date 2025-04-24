package ru.chousik.web.taskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chousik.web.taskservice.entity.WorkEntity;

import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity,
        UUID> {}
