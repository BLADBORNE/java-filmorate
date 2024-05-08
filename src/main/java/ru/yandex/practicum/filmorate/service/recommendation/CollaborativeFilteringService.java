package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendationByUsers(Integer userId) {
        if (userStorage.getLikedFilmsId(userId).isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> userIds = getSortedUsersBySimilarity(userId);
        if (userIds.isEmpty()) {

            return Collections.emptyList();
        }

        List<Integer> userFilms = userStorage.getLikedFilmsId(userId);

        Set<Integer> recommendationIds = new HashSet<>(userStorage.getLikedFilmsId(userIds.get(0)));

        userFilms.forEach(recommendationIds::remove);
        List<Film> recommendations = new ArrayList<>();
        recommendationIds.forEach(el -> recommendations.add(filmStorage.getFilmById(el)));

        return recommendations;
    }

    private List<Integer> getSortedUsersBySimilarity(Integer userId) {
        List<User> users = userStorage.getUsers().stream()
                .filter(x -> !x.equals(userStorage.getUserById(userId)))
                .collect(Collectors.toList());

        List<Integer> userIds = new ArrayList<>();

        for (User user : users) {
            userIds.add(user.getId());
        }

        userIds.sort(Comparator.comparing(id -> findHowSimilar(userId, id)));

        userIds.removeIf(id -> {
            double similarity = findHowSimilar(userId, id);
            return similarity == 0;
        });

        return userIds;
    }

    private double findHowSimilar(Integer userId1, Integer userId2) {
        List<Integer> user1Scores = userStorage.getScoreVectorByUserId(userId1);
        List<Integer> user2Scores = userStorage.getScoreVectorByUserId(userId2);

        return findCosineSimiliraty(user1Scores, user2Scores);
    }

    private double findCosineSimiliraty(List<Integer> vectorA, List<Integer> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        return dotProduct / (normA * normB);
    }
}
