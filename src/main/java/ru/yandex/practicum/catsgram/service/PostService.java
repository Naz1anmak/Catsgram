package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
@RequiredArgsConstructor
public class PostService {
    private final UserService userService;

    private final Map<Long, Post> posts = new HashMap<>();

    public Collection<Post> findAll(SortOrder sort, int from, int size) {
        if (from < 0) {
            throw new ConditionsNotMetException("Параметр from не может быть меньше нуля");
        }
        if (size <= 0) {
            throw new ConditionsNotMetException("Параметр size должен быть больше нуля");
        }

        Comparator<Post> comparator = Comparator.comparing(Post::getPostDate);
        if (sort == SortOrder.DESCENDING) {
            comparator = comparator.reversed();
        }

        return posts.values().stream()
                .sorted(comparator)
                .skip(from)
                .limit(size)
                .toList();
    }

    public Post getPost(Long id) {
        return Optional.ofNullable(posts.get(id))
                .orElseThrow(() -> new NotFoundException("Пост с id = " + id + " не найден"));
    }

    public Post create(Post post) {
        userService.findUserById(post.getAuthorId())
                .orElseThrow(() -> new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден"));

        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}