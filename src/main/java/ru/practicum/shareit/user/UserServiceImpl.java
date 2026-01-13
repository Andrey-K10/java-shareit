package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();

    private Long userIdSequence = 1L;

    @Override
    public User getUser(Long id) {
        return users.get(id);
    }

    @Override
    public User addUser(User user) {

        if (user.getEmail() == null
                || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException();
        }

        for (User existing : users.values()) {
            if (existing.getEmail() != null
                    && existing.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new IllegalStateException();
            }
        }

        user.setId(userIdSequence++);

        users.put(user.getId(), user);

        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User updateUser(Long id, User user) {

        User existing = users.get(id);

        if (existing == null) {
            throw new NoSuchElementException();
        }

        if (user.getEmail() != null) {

            if (!user.getEmail().contains("@")) {
                throw new IllegalArgumentException();
            }

            for (User other : users.values()) {
                if (!other.getId().equals(id)
                        && other.getEmail() != null
                        && other.getEmail().equalsIgnoreCase(user.getEmail())) {
                    throw new IllegalStateException();
                }
            }

            existing.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            existing.setName(user.getName());
        }

        return existing;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }
}
