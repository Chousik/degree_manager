package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Rental;

import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {
}
