package ru.chousik.web.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.services.AccountService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.frontend-base-url=http://frontend")
class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountServiceImpl;

    @Test
    void register_createsUser() throws Exception {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("alice");
        dto.setPassword("password123");
        dto.setEmail("alice@example.com");
        dto.setName("Alice");
        dto.setSurname("Doe");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isCreated());

        verify(accountServiceImpl).register(dto);
    }

    @Test
    void verifyEmailGet_redirectsToFrontend() throws Exception {
        mockMvc.perform(get("/api/users/verify-email")
                        .param("token", "token"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://frontend/login?verified=true"));

        verify(accountServiceImpl).verifyEmail("token");
    }

    @Test
    void getUsers_returnsList() throws Exception {
        when(accountServiceImpl.getUsers())
                .thenReturn(List.of(new UserDTO("user-1", List.of("ROLE_USER"))));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user-1"));
    }
}
