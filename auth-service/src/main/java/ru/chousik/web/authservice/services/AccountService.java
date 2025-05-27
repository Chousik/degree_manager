package ru.chousik.web.authservice.services;

import ru.chousik.web.authservice.dto.AdminChangePasswordDto;
import ru.chousik.web.authservice.dto.ChangePasswordDto;
import ru.chousik.web.authservice.dto.RegisterUserDto;

public interface AccountService {
    void register(RegisterUserDto dto);
    void changeOwnPassword(String username, ChangePasswordDto dto);
    void changeUserPassword(String username, AdminChangePasswordDto dto);
    void addAdminRole(String username);
}
