package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable(value = "id") Integer id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createNewDirector(@Valid @RequestBody Director director) {
        return directorService.createNewDirector(director);
    }

    @PutMapping
    public Director updateNewDirector(@Valid @RequestBody Director director) {
        return directorService.updateNewDirector(director);
    }

    @DeleteMapping("/{id}")
    public Director deleteDirector(@PathVariable Integer id) {
        return directorService.deleteDirector(id);
    }
}
