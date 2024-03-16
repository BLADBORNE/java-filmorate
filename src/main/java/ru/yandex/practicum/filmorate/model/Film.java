package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
public class Film {
    private int id;
    @NotBlank
    private final String name;
    @Size(max = 200)
    private final String description;
    private LocalDate releaseDate;
    @Min(1)
    private final int duration;
    private Set<Integer> filmLikes;
}