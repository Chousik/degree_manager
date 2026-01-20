package ru.chousik.web.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import ru.chousik.web.authservice.security.DegreeUserDetails;
import ru.chousik.web.authservice.services.TwoFactorService;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {
    @Value("${app.oauth2.success-redirect:https://fixly-meow.ru}")
    private String oauthSuccessRedirect;

    @Value("${app.oauth2.failure-redirect:https://fixly-meow.ru/login?error=oauth}")
    private String oauthFailureRedirect;

    @Value("${app.oauth2.allowed-origins:*}")
    private String allowedOrigins;

    private final TwoFactorService twoFactorService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofHours(1));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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
        http.getConfigurer(
                OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
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
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .securityMatcher("/api/users/**", "/users/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/api/users/register",
                                        "/api/users/verify-email",
                                        "/users/register",
                                        "/users/verify-email"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder))
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
                                                          AuthenticationSuccessHandler formLoginSuccessHandler,
                                                          AuthenticationFailureHandler formLoginFailureHandler)
            throws Exception {
        http.cors(c -> c.configurationSource(corsConfigurationSource()))
            .formLogin(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler((req, res, auth) -> res.sendRedirect(oauthSuccessRedirect))
                        .failureHandler((req, res, ex) -> res.sendRedirect(oauthFailureRedirect))
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler)
                        .failureHandler(formLoginFailureHandler)
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("v3/**").permitAll()
                        .requestMatchers("/swagger-ui*/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler formLoginFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
            String message = "BAD_CREDENTIALS";
            if (exception != null) {
                if ("OTP_REQUIRED".equals(exception.getMessage())) {
                    message = "OTP_REQUIRED";
                } else if ("INVALID_OTP".equals(exception.getMessage())) {
                    message = "INVALID_OTP";
                }
            }
            response.getWriter().write(message);
        };
    }

    @Bean
    public AuthenticationSuccessHandler formLoginSuccessHandler(AuthenticationFailureHandler failureHandler) {
        return (request, response, authentication) -> {
            Object principal = authentication.getPrincipal();
            if (principal instanceof DegreeUserDetails details) {
                try {
                    twoFactorService.requireCodeForLogin(details.getUser(), request.getParameter("otp"));
                } catch (BadCredentialsException ex) {
                    SecurityContextHolder.clearContext();
                    failureHandler.onAuthenticationFailure(request, response, ex);
                    return;
                }
            }
            response.setStatus(HttpStatus.OK.value());
        };
    }
}
