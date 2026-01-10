package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto addUser(@RequestBody User user) {
        return UserMapper.toUserDto(userService.addUser(user));
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        return UserMapper.toUserDto(userService.getUser(userId));
    }

    @GetMapping
    public List<UserDto> getUsers() {

        List<UserDto> result = new ArrayList<>();

        for (User user : userService.getUsers()) {
            result.add(UserMapper.toUserDto(user));
        }

        return result;
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody User user) {
        return UserMapper.toUserDto(userService.updateUser(userId, user));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
