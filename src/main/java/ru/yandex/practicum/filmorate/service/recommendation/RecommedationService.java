package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommedationService {
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final UserStorage userStorage;

    public List<Film> getRecommendation(Integer userId) {
        userStorage.getUserById(userId);

        List<Film> recommendations = collaborativeFilteringService.getRecommendationByUsers(userId);

        return recommendations.stream().distinct().collect(Collectors.toList());
    }
}
