package ru.chousik.web.authservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import ru.chousik.web.authservice.entity.JwkEntity;
import ru.chousik.web.authservice.repository.JwkRepository;
import ru.chousik.web.authservice.repository.UserProfileRepository;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class Oauth2Config {
    private final UserProfileRepository userProfileRepository;

    public Oauth2Config(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository jdbcRegisteredClientRepository =
                new JdbcRegisteredClientRepository(jdbcTemplate);
        RegisteredClient existing = jdbcRegisteredClientRepository.findByClientId("client");
        if (Objects.isNull(existing)) {
            RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("client")
                    .clientSecret(passwordEncoder.encode("secret"))
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:5173/auth-callback")
                    .redirectUri("http://localhost:5174/auth-callback")
                    .scope(OidcScopes.OPENID)
                    .scope("offline_access")
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(30))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .reuseRefreshTokens(true)
                            .build())
                    .build();
            jdbcRegisteredClientRepository.save(client);
        } else {
            var builder = RegisteredClient.from(existing);
            builder.redirectUri("http://localhost:5173/auth-callback");
            builder.redirectUri("http://localhost:5174/auth-callback");
            jdbcRegisteredClientRepository.save(builder.build());
        }
        return jdbcRegisteredClientRepository;
    }

    @Bean
    @Transactional
    public JWKSource<SecurityContext> jwkSource(JwkRepository jwkRepository,
                                                final @Value("${jwk.name}") String JWK_ID)
            throws NoSuchAlgorithmException {
        RSAKey rsaKey;
        Optional<JwkEntity> jwkEntityOptional = jwkRepository.getJwkEntitiesById(JWK_ID);
        if (jwkEntityOptional.isPresent()) {
            rsaKey = jwkEntityOptional.get().getRsaKey();
        } else {
            KeyPairGenerator keyPairGenerator
                    = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
            JwkEntity entity = new JwkEntity(JWK_ID, rsaKey);
            jwkRepository.save(entity);
        }
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtRoleCustomizer() {
        return context -> {
            if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(
                    context.getAuthorizationGrantType()) ||
                    AuthorizationGrantType.REFRESH_TOKEN.equals(
                            context.getAuthorizationGrantType())) {

                Authentication principal = context.getPrincipal();
                var roles = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toCollection(ArrayList::new));
                context.getClaims().claim("roles", roles);
                addUserIdClaim(context);
                context.getClaims().subject(principal.getName());
            }
        };
    }

    private void addUserIdClaim(JwtEncodingContext context) {
        String username = context.getPrincipal().getName();
        userProfileRepository.findByUsername(username).ifPresentOrElse(
                profile -> context.getClaims().claim("user_id", profile.getId().toString()),
                () -> context.getClaims().claim("user_id", username)
        );
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }
}
