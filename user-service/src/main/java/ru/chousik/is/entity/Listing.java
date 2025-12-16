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
@Table(name = "listing")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Size(max = 500)
    @NotNull
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @Column(name = "price_per_hour", nullable = false, precision = 38, scale = 10)
    private BigDecimal pricePerHour;

    @Column(name = "deposit_amount", precision = 38, scale = 10)
    private BigDecimal depositAmount;

    @Column(name = "auto_confirmation")
    private Boolean autoConfirmation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ListingStatus status;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "is_flagged")
    private Boolean flagged;

    @Column(name = "flag_reason", length = 1000)
    private String flagReason;

}
