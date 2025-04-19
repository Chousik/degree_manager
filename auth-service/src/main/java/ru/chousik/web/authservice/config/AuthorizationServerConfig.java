package ru.chousik.web.authservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {
    //На время теста отрубил
//    @Bean
//    public UserDetailsManager userDetailsManager(DataSource dataSource){
//        return new JdbcUserDetailsManager(dataSource);
//    }
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
    @Bean
    public UserDetailsService uds() {
        var uds = new InMemoryUserDetailsManager();

        var u = User.withUsername("chousik")
                .password("chousik")
                .authorities("read")
                .build();

        uds.createUser(u);

        return uds;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
    @Bean
    @Order(0)
    public SecurityFilterChain asFilterChain(HttpSecurity http)
            throws Exception {
        var authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        //Включение потокола OpenID Connect
        http.getConfigurer(
                OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        //Буквально страницу авторизации
        http.exceptionHandling(e->
                e.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login")
                ));
        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.formLogin(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(
                c -> c.anyRequest().authenticated());
        return http.build();
    }
    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("client")
                .clientSecret(passwordEncoder.encode("secret"))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://www.manning.com/authorized")
                .scope(OidcScopes.OPENID)
                .scope("offline_access")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(true)
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(client);
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource()
            throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator
                = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(){
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
                        .toList();
                context.getClaims().claim("roles", roles);
            }
        };
    }
    @Bean
    @Order(1)
    public SecurityFilterChain registerSecurity(HttpSecurity http,
                                                JwtDecoder jwtDecoder) throws Exception {
        http
                .securityMatcher("/hello")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder))
                );

        return http.build();
    }
}
