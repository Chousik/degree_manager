package ru.chousik.web.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.exception.SelfRoleModificationException;
import ru.chousik.web.authservice.services.AccountService;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping("/users")
public class AccountController {
    final AccountService accountServiceImpl;
    @Value("${app.frontend-base-url:http://localhost:5173}")
    String frontendBaseUrl;
    
    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешно создан."),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким ником или email уже существует.")
    })
    public ResponseEntity<?> register(@Parameter(name = "registerDTO",
    description = "Содержит логин, пароль и данные пользователя.")
            @RequestBody @Valid RegisterUserDTO dto){
        accountServiceImpl.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Подтверждение email по токену.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email подтверждён."),
            @ApiResponse(responseCode = "400", description = "Токен невалиден или истёк.")
    })
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        accountServiceImpl.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Подтверждение email по токену (GET для ссылок из письма).")
    public ResponseEntity<?> verifyEmailGet(@RequestParam("token") String token) {
        accountServiceImpl.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, frontendBaseUrl + "/login?verified=true")
                .build();
    }

    @PostMapping("/password")
    @Operation(summary = "Смена пароля юзером.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пароль успешно сменен."),
            @ApiResponse(responseCode = "400", description = "Старый пароль введен неверно."),
            @ApiResponse(responseCode = "400", description = "Новый и старый пароли совпадают."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> changeOwnPassword(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(name = "changePasswordDTO",
                    description = "Содержит старый и новый пароль.")
            @RequestBody
            @Valid ChangePasswordDTO dto){
        accountServiceImpl.changeOwnPassword(resolveUsername(jwt), dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удаление пользователя админом.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален."),
            @ApiResponse(responseCode = "404", description = "Требуемый пользователь не найден."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}/remove")
    public ResponseEntity<?> removeUser(
            @Parameter(name = "username",
                    description = "Никнейм пользователя.")
            @PathVariable String username){
        accountServiceImpl.deleteUser(username);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/password")
    @Operation(summary = "Смена пароля админом.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пароль успешно сменен."),
            @ApiResponse(responseCode = "404", description = "Требуемый пользователь не найден."),
            @ApiResponse(responseCode = "400", description = "Новый и старый пароли совпадают."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> changeUserPassword(
            @Parameter(name = "username",
                    description = "Никнейм пользователя.")
            @PathVariable String username,
            @Parameter(name = "password",
                    description = "Новый пароль")
            @RequestBody @Valid AdminChangePasswordDTO dto) {
        accountServiceImpl.changeUserPassword(username, dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/setadmin")
    @Operation(summary = "Добавление роли админа юзеру.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Роль админа успешно добавлена."),
            @ApiResponse(responseCode = "409", description = "У пользователя уже есть роль админа."),
            @ApiResponse(responseCode = "404", description = "Требуемый пользователь не найден."),
            @ApiResponse(responseCode = "403", description = "Запрещено модифицировать свои роли."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> setAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(name = "username",
                    description = "Никнейм пользователя")
            @PathVariable String username) {
        if (username.equals(userDetails.getUsername())){
            throw new SelfRoleModificationException();
        }
        accountServiceImpl.addAdminRole(username);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/unsetadmin")
    @Operation(summary = "Добавление роли админа юзеру.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Роль админа успешно добавлена."),
            @ApiResponse(responseCode = "400", description = "У пользователя отсутствует роль админа."),
            @ApiResponse(responseCode = "403", description = "Запрещено модифицировать свои роли."),
            @ApiResponse(responseCode = "404", description = "Требуемый пользователь не найден."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> unsetAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(name = "username",
                    description = "Никнейм пользователя")
            @PathVariable String username) {
        if (username.equals(userDetails.getUsername())){
            throw new SelfRoleModificationException();
        }
        accountServiceImpl.removeAdminRole(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получение юзеров.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    @GetMapping
    public List<UserDTO> getUsers(){
        return accountServiceImpl.getUsers();
    }

    private String resolveUsername(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Authentication required");
        }
        String preferred = jwt.getClaimAsString("preferred_username");
        if (StringUtils.hasText(preferred)) {
            return preferred;
        }
        String subject = jwt.getSubject();
        if (StringUtils.hasText(subject)) {
            return subject;
        }
        String claimUser = jwt.getClaimAsString("user_name");
        if (StringUtils.hasText(claimUser)) {
            return claimUser;
        }
        throw new IllegalStateException("Unable to resolve username from token");
    }
}
