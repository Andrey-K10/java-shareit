package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addUser_whenValid_shouldSaveUser() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");

        User savedUser = new User(1L, "John Doe", "john@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.addUser(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void addUser_whenEmailExists_throwConflictException() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");

        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> userService.addUser(user));
        verify(userRepository).save(user);
    }

    @Test
    void getUser_whenUserExists_shouldReturnUser() {
        Long userId = 1L;
        User user = new User(userId, "John Doe", "john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getUser_whenUserNotFound_throwNotFoundException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = new User(1L, "John", "john@example.com");
        User user2 = new User(2L, "Jane", "jane@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_whenValid_shouldUpdateAllFields() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User();
        updateData.setName("New Name");
        updateData.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(userId, updateData);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void updateUser_whenUpdateOnlyName_shouldUpdateOnlyName() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User();
        updateData.setName("New Name");
        // email is null

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(userId, updateData);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
    }

    @Test
    void updateUser_whenUpdateOnlyEmail_shouldUpdateOnlyEmail() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User();
        updateData.setEmail("new@example.com");
        // name is null

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(userId, updateData);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Old Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void updateUser_whenUpdateWithNullFields_shouldNotChange() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User();
        // both name and email are null

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(userId, updateData);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Old Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
    }

    @Test
    void updateUser_whenUserNotFound_throwNotFoundException() {
        Long userId = 999L;
        User updateData = new User();
        updateData.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, updateData));
    }

    @Test
    void updateUser_whenEmailExists_throwConflictException() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User();
        updateData.setEmail("existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, updateData));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        Long userId = 1L;
        User user = new User(userId, "John", "john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_whenUserNotFound_throwNotFoundException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void updateUser_withEmptyDto_shouldNotChangeFields() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        User updateData = new User(); // пустой объект

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(userId, updateData);

        assertEquals("Old Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
    }
}