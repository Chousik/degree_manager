package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.BanList;

import java.util.List;
import java.util.UUID;

public interface BanListRepository extends JpaRepository<BanList, UUID> {
    List<BanList> findAllByStatusIgnoreCase(String status);
    List<BanList> findAllByBannedUser_Id(UUID userId);
}
