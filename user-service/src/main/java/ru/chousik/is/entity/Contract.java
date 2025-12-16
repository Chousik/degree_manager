package ru.chousik.is.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @Size(max = 30)
    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "signed_at")
    private OffsetDateTime signedAt;

    @Size(max = 255)
    @Column(name = "file_url")
    private String fileUrl;

    @Size(max = 255)
    @Column(name = "signature_hash")
    private String signatureHash;

}