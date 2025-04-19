package ru.chousik.web.authservice.entity;

import com.nimbusds.jose.jwk.RSAKey;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "jwk_story")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JwkEntity {
    @Id
    String id;
    RSAKey rsaKey;
}
