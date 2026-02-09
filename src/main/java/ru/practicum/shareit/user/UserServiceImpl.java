package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User addUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        boolean emailExists = userRepository.findAll().stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ConflictException("Email already exists");
        }

        return userRepository.save(user);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException());

        if (user.getEmail() != null) {
            if (!user.getEmail().contains("@")) {
                throw new IllegalArgumentException();
            }

            if (userRepository.findAll().stream()
                    .anyMatch(other -> !other.getId().equals(id)
                            && other.getEmail().equalsIgnoreCase(user.getEmail()))) {
                throw new ConflictException("Email already exists");
            }

            existing.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            existing.setName(user.getName());
        }

        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}