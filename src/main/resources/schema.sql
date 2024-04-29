DROP TABLE IF EXISTS
    genres, users, ratings, films, films,user_friend, film_genre, film_like, director, film_director;

CREATE TABLE IF NOT EXISTS ratings
(
    rating_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    rating    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         VARCHAR(100) NOT NULL CHECK (name <> ''),
    description  VARCHAR(200) NOT NULL CHECK LENGTH(description) <= 200,
    release_date DATE         NOT NULL CHECK (release_date >= DATE '1895-12-28'),
    duration     INTEGER      NOT NULL CHECK (duration > 0),
    rating_id    INTEGER      NOT NULL REFERENCES ratings (rating_id) ON DELETE CASCADE
);

CREATE table IF NOT EXISTS genres
(
    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    genre    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    user_id  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email    VARCHAR(100) NOT NULL UNIQUE CHECK (email <> ''),
    login    VARCHAR(100) NOT NULL UNIQUE CHECK (login <> ''),
    name     VARCHAR(100),
    birthday DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS user_friend
(
    sender_id         INTEGER     NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    recipients_id     INTEGER     NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    friendship_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (sender_id, recipients_id)
);

CREATE TABLE IF NOT EXISTS film_like
(
    film_id INTEGER NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genre
(
    film_id  INTEGER NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres (genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS director
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         VARCHAR(100) NOT NULL CHECK (name <> '')
);

CREATE TABLE IF NOT EXISTS film_director
(
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_id INTEGER NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    director_id INTEGER NOT NULL REFERENCES director (id) ON DELETE CASCADE
)