package ru.chousik.web.authservice.services;

import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;

import java.util.List;

public interface AccountService {
    void register(RegisterUserDTO dto);
    void changeOwnPassword(String username, ChangePasswordDTO dto);
    void changeUserPassword(String username, AdminChangePasswordDTO dto);
    void addAdminRole(String username);
    void removeAdminRole(String username);
    List<UserDTO> getUsers();
}
