package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import ru.chousik.is.config.PaymentProviderProperties;
import ru.chousik.is.entity.Payment;
import ru.chousik.is.entity.PaymentPurpose;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.PaymentRepository;
import ru.chousik.is.repository.RentalRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PROVIDER_URL = "https://api.yookassa.ru/v3";
    private static final String CURRENCY_RUB = "RUB";

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentProviderProperties properties;
    private final WebClient webClient = WebClient.builder()
            .baseUrl(PROVIDER_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Transactional
    public Payment preparePaymentRecord(Rental rental, PaymentPurpose purpose, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Amount must be positive for payment " + purpose);
        }
        Payment existing = paymentRepository.findFirstByRental_IdAndPurpose(rental.getId(), purpose);
        if (existing != null) {
            existing.setAmount(amount);
            existing.setCurrency(CURRENCY_RUB);
            if (existing.getPurpose() == null) {
                existing.setPurpose(purpose);
            }
            existing.setStatus(existing.getStatus() == null ? "pending" : existing.getStatus());
            return paymentRepository.save(existing);
        }
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setAmount(amount);
        payment.setCurrency(CURRENCY_RUB);
        payment.setPurpose(purpose);
        payment.setStatus("pending");
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment initiatePayment(UUID rentalId, PaymentPurpose purpose, String returnUrl) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(rentalId)));
        BigDecimal amount = resolveAmount(rental, purpose);
        Payment payment = preparePaymentRecord(rental, purpose, amount);
        Assert.notNull(properties.getShopId(), "YooKassa shopId is not configured");
        Assert.notNull(properties.getSecretKey(), "YooKassa secretKey is not configured");

        String auth = properties.getShopId() + ":" + properties.getSecretKey();
        String idempotenceKey = UUID.randomUUID().toString();

        String confirmationReturnUrl = returnUrl != null ? returnUrl : properties.getReturnBaseUrl();
        if (confirmationReturnUrl == null) {
            throw new BusinessValidationException("Return URL must be provided for redirect payments");
        }

        var body = Map.of(
                "amount", Map.of(
                        "value", formatAmount(amount),
                        "currency", CURRENCY_RUB
                ),
                "capture", true,
                "description", buildDescription(rental, purpose),
                "confirmation", Map.of(
                        "type", "redirect",
                        "return_url", confirmationReturnUrl
                )
        );

        YooPaymentResponse response = webClient.post()
                .uri("/payments")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes()))
                .header("Idempotence-Key", idempotenceKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(YooPaymentResponse.class)
                .block();

        if (response == null || response.id() == null) {
            throw new BusinessValidationException("Не удалось создать платеж в YooKassa");
        }

        payment.setExternalId(response.id());
        payment.setStatus(response.status());
        payment.setConfirmationUrl(response.confirmation() != null ? response.confirmation().confirmation_url() : null);
        if ("succeeded".equalsIgnoreCase(response.status())) {
            payment.setPaidAt(OffsetDateTime.now());
        }
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refreshStatus(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment %s not found".formatted(paymentId)));
        if (payment.getExternalId() == null) {
            throw new BusinessValidationException("Payment was not initiated in provider");
        }
        Assert.notNull(properties.getShopId(), "YooKassa shopId is not configured");
        Assert.notNull(properties.getSecretKey(), "YooKassa secretKey is not configured");
        String auth = properties.getShopId() + ":" + properties.getSecretKey();

        YooPaymentResponse response = webClient.get()
                .uri("/payments/{id}", payment.getExternalId())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes()))
                .retrieve()
                .bodyToMono(YooPaymentResponse.class)
                .block();

        if (response != null) {
            payment.setStatus(response.status());
            if (response.paid() != null && response.paid() && payment.getPaidAt() == null) {
                payment.setPaidAt(OffsetDateTime.now());
            }
            if (response.confirmation() != null && payment.getConfirmationUrl() == null) {
                payment.setConfirmationUrl(response.confirmation().confirmation_url());
            }
        }
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundDepositIfCompleted(Rental rental) {
        Payment deposit = paymentRepository.findFirstByRental_IdAndPurpose(rental.getId(), PaymentPurpose.DEPOSIT);
        if (deposit == null || deposit.getExternalId() == null) {
            return deposit;
        }
        if (!Objects.equals(deposit.getStatus(), "succeeded")) {
            deposit = refreshStatus(deposit.getId());
        }
        if (!Objects.equals(deposit.getStatus(), "succeeded")) {
            return deposit;
        }
        Assert.notNull(properties.getShopId(), "YooKassa shopId is not configured");
        Assert.notNull(properties.getSecretKey(), "YooKassa secretKey is not configured");
        String auth = properties.getShopId() + ":" + properties.getSecretKey();

        var body = Map.of(
                "amount", Map.of(
                        "value", formatAmount(deposit.getAmount()),
                        "currency", deposit.getCurrency() == null ? CURRENCY_RUB : deposit.getCurrency()
                ),
                "description", "Возврат депозита по аренде " + rental.getId()
        );

        webClient.post()
                .uri("/payments/{id}/refunds", deposit.getExternalId())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes()))
                .header("Idempotence-Key", UUID.randomUUID().toString())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        deposit.setStatus("refunded");
        deposit.setRefundedAt(OffsetDateTime.now());
        return paymentRepository.save(deposit);
    }

    private BigDecimal resolveAmount(Rental rental, PaymentPurpose purpose) {
        if (purpose == PaymentPurpose.RENTAL) {
            return rental.getTotalAmount();
        }
        return rental.getDepositAmount();
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String buildDescription(Rental rental, PaymentPurpose purpose) {
        return purpose == PaymentPurpose.DEPOSIT
                ? "Депозит за аренду " + rental.getId()
                : "Оплата аренды " + rental.getId();
    }

    private record YooPaymentResponse(String id,
                                      String status,
                                      Boolean paid,
                                      Confirmation confirmation) {
    }

    private record Confirmation(String type, String confirmation_url) {
    }
}
