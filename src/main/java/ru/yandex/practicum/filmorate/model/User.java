package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class User {
    private int id;
    @Email
    private String email;
    @NotEmpty
    @NotBlank
    private final String login;
    private String name;
    private final LocalDate birthday;
}
