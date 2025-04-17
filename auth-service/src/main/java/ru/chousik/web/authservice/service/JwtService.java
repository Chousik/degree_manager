package ru.chousik.web.authservice.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE)
public class JwtService {

    @Value("classpath:keys/jwt.key")
    Resource privateKey;

    private RSAPrivateKey rsaPrivateKey() throws Exception {
        try (var is = privateKey.getInputStream()) {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(is.readAllBytes()));
        }
    }

    public String generateToken(UserDetails user) throws Exception {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer("auth-service")
                .withSubject(user.getUsername())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(15, ChronoUnit.MINUTES)))
                .withClaim("roles", user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .sign(Algorithm.RSA256(null, rsaPrivateKey()));
    }
}

