package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dao.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id);
    }

    public Director createNewDirector(Director director) {
        return directorStorage.createNewDirector(director);
    }

    public Director updateNewDirector(Director director) {
        return directorStorage.updateNewDirector(director);
    }
}
