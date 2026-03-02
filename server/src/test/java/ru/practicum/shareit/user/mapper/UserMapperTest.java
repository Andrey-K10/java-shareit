package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_shouldMapAllFields() {
        User user = new User(1L, "John Doe", "john@example.com");

        UserDto dto = UserMapper.toUserDto(user);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void toUserDto_whenUserNull_shouldReturnNull() {
        UserDto dto = UserMapper.toUserDto(null);
        assertNull(dto);
    }

    @Test
    void toUser_shouldMapAllFields() {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");

        User user = UserMapper.toUser(dto);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void toUser_whenDtoNull_shouldReturnNull() {
        User user = UserMapper.toUser(null);
        assertNull(user);
    }
}