package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void addUser_whenValid_shouldReturnCreated() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");

        User user = UserMapper.toUser(requestDto);
        User savedUser = new User(1L, "John Doe", "john@example.com");

        when(userService.addUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void addUser_whenEmailExists_shouldReturnConflict() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");

        when(userService.addUser(any(User.class))).thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "John Doe", "john@example.com");

        when(userService.getUser(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUser_whenNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 999L;

        when(userService.getUser(userId)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_whenUsersExist_shouldReturnOk() throws Exception {
        User user1 = new User(1L, "John", "john@example.com");
        User user2 = new User(2L, "Jane", "jane@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateUser_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated Name");
        requestDto.setEmail("updated@example.com");

        User updatedUser = new User(userId, "Updated Name", "updated@example.com");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 999L;
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated Name");

        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_whenEmailExists_shouldReturnConflict() throws Exception {
        Long userId = 1L;
        UserDto requestDto = new UserDto();
        requestDto.setEmail("existing@example.com");

        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_whenValid_shouldReturnNoContent() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 999L;

        doThrow(new NotFoundException("User not found")).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }
}