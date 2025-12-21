package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE)
    private String email;

    @Size(max = 20)
    @NotNull
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Size(max = 60)
    @NotNull
    @Column(name = "surname", nullable = false, length = 60)
    private String surname;

    @Size(max = 20)
    @Column(name = "last_name", length = 20)
    private String lastName;

    @Size(max = 12)
    @Column(name = "phone", length = 12)
    private String phone;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 60)
    @NotNull
    @Column(name = "city", nullable = false, length = 60)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_ban")
    private BanList lastBan;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

}
