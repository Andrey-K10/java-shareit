package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User addUser(User user);

    User getUser(Long id);

    List<User> getUsers();

    User updateUser(Long id, User user);

    void deleteUser(Long id);

}
