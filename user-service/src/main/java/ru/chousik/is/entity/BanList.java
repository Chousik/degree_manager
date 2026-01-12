package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ban_list")
public class BanList {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banned_user_id", nullable = false)
    private User bannedUser;

    @Size(max = 1000)
    @Column(name = "ban_reason", length = 1000)
    private String banReason;

    @Size(max = 30)
    @Column(name = "ban_type", length = 30)
    private String banType;

    @Column(name = "ban_duration")
    private OffsetDateTime banDuration;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
