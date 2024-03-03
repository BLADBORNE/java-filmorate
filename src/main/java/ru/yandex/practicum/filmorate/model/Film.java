package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    @NotEmpty
    private final String name;
    @Size(max = 200)
    private final String description;
    private LocalDate releaseDate;
    @Min(1)
    private final int duration;
}