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
    @Operation(summary = "Create users by Admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "409", description = "User already exist"),
            @ApiResponse(responseCode = "403", description = "You need Admin role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> register(@Parameter(name = "username&password",
    description = "dto with username and password")
            @RequestBody @Valid RegisterUserDTO dto){
        try {
            accountServiceImpl.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/password")
    @Operation(summary = "Change own password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Old password is incorrect"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> changeOwnPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(name = "oldPassword") @RequestBody
            @Valid ChangePasswordDTO dto){
        try {
            accountServiceImpl.changeOwnPassword(userDetails.getUsername(), dto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}/remove")
    public ResponseEntity<?> removeUser(
            @PathVariable String username){
        try {
            accountServiceImpl.deleteUser(username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/password")
    @Operation(summary = "Change user password by Admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "You need Admin role")
    })
    public ResponseEntity<?> changeUserPassword(
            @Parameter(description = "Username to change password for")
            @PathVariable String username,
            @Parameter(description = "New password")
            @RequestBody @Valid AdminChangePasswordDTO dto) {
        try {
            accountServiceImpl.changeUserPassword(username, dto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/setadmin")
    @Operation(summary = "Add role Admin to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role added successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "You need Admin role")
    })
    public ResponseEntity<?> setAdmin(
            @Parameter(description = "Username to grant admin role")
            @PathVariable String username) {
        try {
            accountServiceImpl.addAdminRole(username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{username}/unsetadmin")
    public ResponseEntity<?> unsetAdmin(
            @Parameter(description = "Username to ungrant admin role")
            @PathVariable String username) {
        try {
            accountServiceImpl.removeAdminRole(username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<UserDTO> getUsers(){
        return accountServiceImpl.getUsers();
    }
}
