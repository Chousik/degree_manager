package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.ModerationAction;

import java.util.Optional;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {
    Optional<ModerationAction> findTopByReport_IdOrderByCreatedAtDesc(UUID reportId);
}
