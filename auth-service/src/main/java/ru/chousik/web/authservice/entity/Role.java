package ru.chousik.web.authservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Table(name = "role")
@Entity
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "role", nullable = false)
    String roleName;

    @Column(name = "description")
    String description;
}
