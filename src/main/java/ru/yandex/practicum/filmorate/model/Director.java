package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Director {
    private int id;
    @NotBlank
    private final String name;
}
