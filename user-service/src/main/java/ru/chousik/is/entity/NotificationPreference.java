package ru.chousik.is.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notification_preference")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "system_notifications")
    private Boolean systemNotifications;

    @Column(name = "rental_notifications")
    private Boolean rentalNotifications;

    @Column(name = "message_notifications")
    private Boolean messageNotifications;

    @Column(name = "payment_notifications")
    private Boolean paymentNotifications;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
