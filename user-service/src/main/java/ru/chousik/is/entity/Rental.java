package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "rental")
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessor_id")
    private User lessor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessee_id")
    private User lessee;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private RentalStatus status;

    @Column(name = "total_amount", precision = 38, scale = 10)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", precision = 38, scale = 10)
    private BigDecimal depositAmount;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

/*
 TODO [Amplicode] create field to map the 'period' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "period", columnDefinition = "tstzrange(0, 0)")
    private Object period;
*/
}
