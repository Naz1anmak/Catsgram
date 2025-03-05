package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User getUser(Long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new NotFoundException("Юзер с id = " + id + " не найден"));
    }

    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        checkEmail(user);

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkEmail(User user) {
        for (User value : users.values()) {
            if (value.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
    }

    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            if (!oldUser.getEmail().equals(user.getEmail())) {
                checkEmail(user);
            }

            if (user.getUsername() != null) {
                oldUser.setUsername(user.getUsername());
            }
            if (user.getEmail() != null) {
                oldUser.setEmail(user.getEmail());
            }
            if (user.getPassword() != null) {
                oldUser.setPassword(user.getPassword());
            }
            oldUser.setRegistrationDate(user.getRegistrationDate());
            return oldUser;
        }
        throw new NotFoundException("Юзер с id = " + user.getId() + " не найден");
    }

    public Optional<User> findUserById(Long id) {
        return users.values().stream()
                .filter(user -> Objects.equals(user.getId(), id))
                .findFirst();
    }
}