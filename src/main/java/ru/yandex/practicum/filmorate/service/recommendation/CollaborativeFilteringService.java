package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CollaborativeFilteringService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendationByFriends(Integer userId) {
        List<Integer> friendsId = getSortedFriendsBySimilarity(userId);
        if (friendsId.isEmpty()) {

            return Collections.emptyList();
        }

        List<Integer> userFilms = userStorage.getLikedFilmsId(userId);
        Set<Integer> recommendationId = new HashSet<>();

        for (Integer friendId : friendsId) {
            recommendationId.addAll(userStorage.getLikedFilmsId(friendId));
        }

        recommendationId.retainAll(userFilms);
        List<Film> recommendation = new ArrayList<>();
        recommendationId.forEach(el -> recommendation.add(filmStorage.getFilmById(el)));

        return recommendation;
    }

    private List<Integer> getSortedFriendsBySimilarity(Integer userId) {
        List<User> friends = userStorage.getUsersFriends(userId);
        List<Integer> friendsId = new ArrayList<>();

        for (User friend : friends) {
            friendsId.add(friend.getId());
        }

        friendsId.sort(Comparator.comparing(friendId -> findHowSimilar(userId, friendId)));

        friendsId.removeIf(friendId -> {
            double similarity = findHowSimilar(userId, friendId);
            return similarity == 0 || similarity == 1;
        });

        return friendsId;
    }

    private double findHowSimilar(Integer userId, Integer friendId) {
        List<Film> films = filmStorage.getFilms();
        List<Integer> userLikes = userStorage.getLikedFilmsId(userId);
        List<Integer> friendLikes = userStorage.getLikedFilmsId(friendId);

        double[] userVector = generateLikeVector(films, userLikes);
        double[] friendVector = generateLikeVector(films, friendLikes);

        return findCosineSimiliraty(userVector, friendVector);
    }

    private double[] generateLikeVector(List<Film> allFilms, List<Integer> likedFilms) {
        double[] likeVector = new double[allFilms.size()];

        for (int i = 0; i < allFilms.size(); i++) {
            int movieId = allFilms.get(i).getId();

            if (likedFilms.contains(movieId)) {
                likeVector[i] = 1;
            } else {
                likeVector[i] = 0;
            }
        }

        return likeVector;
    }

    private double findCosineSimiliraty(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        return dotProduct / (normA * normB);
    }
}
