package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.user.dto.UserGatewayDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserClient userClient;

    @Test
    void addUser_whenValid_shouldReturnOk() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        when(userClient.addUser(any(UserGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addUser_whenNameBlank_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("");
        dto.setEmail("john@example.com");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenNameNull_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName(null);
        dto.setEmail("john@example.com");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenEmailBlank_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenEmailNull_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenEmailMissingAt_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("john.example.com");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_whenEmailMissingDomain_shouldReturnBadRequest() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("john@");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_whenValid_shouldReturnOk() throws Exception {
        when(userClient.getUser(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_whenValid_shouldReturnOk() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_whenValid_shouldReturnOk() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("Updated Name");
        dto.setEmail("updated@example.com");

        when(userClient.updateUser(anyLong(), any(UserGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_whenValidWithOnlyName_shouldReturnOk() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("Updated Name");
        dto.setEmail(null);

        when(userClient.updateUser(anyLong(), any(UserGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_whenValidWithOnlyEmail_shouldReturnOk() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName(null);
        dto.setEmail("updated@example.com");

        when(userClient.updateUser(anyLong(), any(UserGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_whenValidWithEmptyBody_shouldReturnOk() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName(null);
        dto.setEmail(null);

        when(userClient.updateUser(anyLong(), any(UserGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_whenValid_shouldReturnOk() throws Exception {
        when(userClient.deleteUser(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

}