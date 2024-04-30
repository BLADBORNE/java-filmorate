package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CollaborativeFilteringService {
    @Autowired
    private final UserStorage userStorage;
    @Autowired
    private final FilmStorage filmStorage;

    public List<Film> getRecommendationByUsers(Integer userId) {
        List<Integer> usersId = getSortedUsersBySimilarity(userId);
        if (usersId.isEmpty()) {

            return Collections.emptyList();
        }

        List<Integer> userFilms = userStorage.getLikedFilmsId(userId);

        Set<Integer> recommendationId = new HashSet<>(userStorage.getLikedFilmsId(usersId.get(0)));

        userFilms.forEach(recommendationId::remove);
        List<Film> recommendation = new ArrayList<>();
        recommendationId.forEach(el -> recommendation.add(filmStorage.getFilmById(el)));

        return recommendation;
    }

    private List<Integer> getSortedUsersBySimilarity(Integer userId) {
        List<User> users = userStorage.getUsers().stream()
                .filter(x -> !x.equals(userStorage.getUserById(userId)))
                .collect(Collectors.toList());

        List<Integer> usersId = new ArrayList<>();

        for (User user : users) {
            usersId.add(user.getId());
        }

        usersId.sort(Comparator.comparing(id -> findHowSimilar(userId, id)));

        usersId.removeIf(id -> {
            double similarity = findHowSimilar(userId, id);
            return similarity == 0;
        });

        return usersId;
    }

    private double findHowSimilar(Integer userId1, Integer userId2) {
        List<Film> films = filmStorage.getFilms();
        List<Integer> user1Likes = userStorage.getLikedFilmsId(userId1);
        List<Integer> user2Likes = userStorage.getLikedFilmsId(userId2);

        double[] userVector = generateLikeVector(films, user1Likes);
        double[] friendVector = generateLikeVector(films, user2Likes);

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
