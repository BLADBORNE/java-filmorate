package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommedationService {
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final ContentBasedFilteringService contentBasedFilteringService;
    private final UserStorage userStorage;

    // В случае низкого количествва друзей
    // Рекомендации тогда будут выводить сначала список фильмов по жанрам
    // которые предпочитает пользователь отфильтрованных по популярности
    // в случае если у пользователя нет лайков - тогда будет выводиться рандомный список фильмов.

    public Set<Film> getRecommendation(Integer userId, int limit) {
        List<User> friends = userStorage.getUsersFriends(userId);
        List<Integer> likedFilms = userStorage.getLikedFilmsId(userId);
        Set<Film> recommendation = new HashSet<>();

        if (friends.size() >= 2 && likedFilms.size() >= 2) {
            recommendation.addAll(collaborativeFilteringService.getRecommendationByFriends(userId));
        }

        if (!likedFilms.isEmpty()) {
            recommendation.addAll(contentBasedFilteringService.getRecommendationByGenre(userId));
        }

        if (likedFilms.isEmpty()) {
            recommendation.addAll(contentBasedFilteringService.getPopularRecommendation());
        }

        return recommendation.stream().limit(limit).collect(Collectors.toSet());
    }
}
