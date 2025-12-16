package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 38, scale = 10)
    private BigDecimal amount;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Size(max = 100)
    @Column(name = "external_id", length = 100)
    private String externalId;

}