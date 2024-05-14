package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final Map<Pair, Double> similarityCache = new HashMap<>();

    public List<Film> getRecommendationByUsers(Integer userId) {
        List<Integer> userFilms = userStorage.getLikedFilmsId(userId);

        if (userFilms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> userIds = getSortedUsersBySimilarity(userId);
        if (userIds.isEmpty()) {

            return Collections.emptyList();
        }

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
            int otherUserId = user.getId();
            double similarity = similarityCache.getOrDefault(new Pair(userId, otherUserId), -1.0);
            if (similarity == -1.0) {
                similarity = findHowSimilar(userId, otherUserId);
                similarityCache.put(new Pair(userId, otherUserId), similarity);
            }

            if (similarity != 0) {
                userIds.add(otherUserId);
            }
        }

        userIds.sort(Comparator.comparing(id -> similarityCache.get(new Pair(userId, id))));

        return userIds;
    }

    private double findHowSimilar(Integer targetUser, Integer otherUser) {
        Map<Integer, Integer> targetUserScores = userStorage.getScoreVectorByUserId(targetUser);
        List<Integer> sortedKeysForTarget = new ArrayList<>(targetUserScores.keySet());
        Collections.sort(sortedKeysForTarget);
        List<Integer> sortedValuesForTarget = new ArrayList<>();
        for (Integer key : sortedKeysForTarget) {
            sortedValuesForTarget.add(targetUserScores.get(key));
        }

        Map<Integer, Integer> otherUserScores = userStorage.getScoreVectorByUserId(otherUser);
        List<Integer> sortedKeysForOtherUser = new ArrayList<>(otherUserScores.keySet());
        Collections.sort(sortedKeysForOtherUser);
        List<Integer> sortedValuesForOtherUser = new ArrayList<>();
        for (Integer key : sortedKeysForOtherUser) {
            sortedValuesForOtherUser.add(otherUserScores.get(key));
        }

        if (!sortedKeysForTarget.equals(sortedKeysForOtherUser)) {
            return 0.0;
        }

        return findCosineSimiliraty(sortedValuesForTarget, sortedValuesForOtherUser);
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

    @Value
    private static class Pair {
        int id;
        int otherId;
    }
}
