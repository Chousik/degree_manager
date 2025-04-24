package ru.chousik.web.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class AuthorizationServerConfig {
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
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain registerSecurity(HttpSecurity http,
                                                JwtDecoder jwtDecoder) throws Exception {
        http
                .securityMatcher("/api/users/**")
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

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.formLogin(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                var config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("POST", "GET", "OPTIONS"));
                config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
                config.setAllowCredentials(true);
                return config;
            })).exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login")          // обрабатываем POST /login
                        .successHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                        })
                        .failureHandler((req, res, exception) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        })
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui*/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
