package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.is.dto.payment.PaymentInitRequest;
import ru.chousik.is.dto.payment.PaymentResponse;
import ru.chousik.is.entity.Payment;
import ru.chousik.is.entity.PaymentPurpose;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.services.PaymentService;
import ru.chousik.is.services.RentalService;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Validated
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RentalService rentalService;

    @PostMapping("/rentals/{rentalId}/initiate")
    public PaymentResponse initiatePayment(@PathVariable UUID rentalId,
                                           @Valid @RequestBody PaymentInitRequest request) {
        Rental rental = rentalService.findRental(rentalId);
        if (rental.getLessee() == null || !rental.getLessee().getId().equals(request.actorId())) {
            throw new BusinessValidationException("Оплату может инициировать только арендатор");
        }
        if (request.purpose() == PaymentPurpose.DEPOSIT && rental.getDepositAmount() == null) {
            throw new BusinessValidationException("Для этой аренды депозит не требуется");
        }
        Payment payment = paymentService.initiatePayment(rentalId, request.purpose(), request.returnUrl());
        return toResponse(payment);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.refreshStatus(paymentId);
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRental() != null ? payment.getRental().getId() : null,
                payment.getPurpose(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getConfirmationUrl(),
                payment.getExternalId(),
                payment.getPaidAt(),
                payment.getRefundedAt()
        );
    }
}
