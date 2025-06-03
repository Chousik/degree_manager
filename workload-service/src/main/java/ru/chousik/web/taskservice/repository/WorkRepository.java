package ru.chousik.web.taskservice.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.chousik.web.taskservice.entity.WorkEntity;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity,
        UUID> {
    @Query(
            value = """
                SELECT w.unique_count
                FROM   works w
                WHERE  w.title = :title
                ORDER  BY w.uuid
                LIMIT  1
                """,
            nativeQuery = true
    )
    Optional<Integer> findFirstUniqueCountByTitle(@Size(max = 500) @NotNull String title);
}
