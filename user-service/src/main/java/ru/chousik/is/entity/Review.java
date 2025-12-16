package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lessor_id")
    private User lessor;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lessee_id")
    private User lessee;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_role", length = 20)
    private ReviewAuthorRole authorRole;

    @NotNull
    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "text", length = Integer.MAX_VALUE)
    private String text;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "is_flagged")
    private Boolean flagged;

    @Column(name = "flag_reason", length = 1000)
    private String flagReason;

}
