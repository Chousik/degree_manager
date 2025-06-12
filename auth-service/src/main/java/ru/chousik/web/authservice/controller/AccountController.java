package ru.chousik.web.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.services.AccountService;

import java.util.List;
import java.util.Map;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AccountController {
    AccountService accountServiceImpl;
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание аккаунта учителя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешно создан."),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким ником существует."),
            @ApiResponse(responseCode = "409", description = "данный учитель уже зарегистрирован."),
            @ApiResponse(responseCode = "404", description = "Введенный учитель не найден в базе."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> register(@Parameter(name = "registerDTO",
    description = "Содержит пароль и логин, а также ФИО учителя.")
            @RequestBody @Valid RegisterUserDTO dto){
        accountServiceImpl.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(name = "changePasswordDTO",
                    description = "Содержит старый и новый пароль.")
            @RequestBody
            @Valid ChangePasswordDTO dto){
        accountServiceImpl.changeOwnPassword(userDetails.getUsername(), dto);
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
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> setAdmin(
            @Parameter(name = "username",
                    description = "Никнейм пользователя")
            @PathVariable String username) {
        accountServiceImpl.addAdminRole(username);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/unsetadmin")
    @Operation(summary = "Добавление роли админа юзеру.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Роль админа успешно добавлена."),
            @ApiResponse(responseCode = "400", description = "У пользователя отсутствует роль админа."),
            @ApiResponse(responseCode = "404", description = "Требуемый пользователь не найден."),
            @ApiResponse(responseCode = "403", description = "Необходима роль админа."),
            @ApiResponse(responseCode = "401", description = "Не пройдена авторизация.")
    })
    public ResponseEntity<?> unsetAdmin(
            @Parameter(name = "username",
                    description = "Никнейм пользователя")
            @PathVariable String username) {
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
}
