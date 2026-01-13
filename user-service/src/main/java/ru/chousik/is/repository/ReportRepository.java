package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Report;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findAllByStatusIgnoreCase(String status);
    List<Report> findAllByTargetTypeIgnoreCase(String targetType);
    List<Report> findAllByTargetTypeIgnoreCaseAndTargetId(String targetType, UUID targetId);
    List<Report> findAllByTargetTypeIgnoreCaseAndTargetIdAndStatusIgnoreCase(String targetType, UUID targetId, String status);
}
