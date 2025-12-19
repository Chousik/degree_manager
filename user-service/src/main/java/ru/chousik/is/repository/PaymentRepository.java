package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Payment;
import ru.chousik.is.entity.PaymentPurpose;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByRental_Id(UUID rentalId);

    Payment findFirstByRental_IdAndPurpose(UUID rentalId, PaymentPurpose purpose);
}
