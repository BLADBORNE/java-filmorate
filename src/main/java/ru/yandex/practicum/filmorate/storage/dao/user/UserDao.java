package ru.yandex.practicum.filmorate.storage.dao.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ru.yandex.practicum.filmorate.service.UserEventFactory.getAddFriendEvent;
import static ru.yandex.practicum.filmorate.service.UserEventFactory.getDeleteFriendEvent;

@Component
@Slf4j
public class UserDao implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final Calendar tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        log.info("Получен запрос на отправку всех пользователей");

        String sql = "SELECT * FROM users";

        log.info("Все пользователи были успешно отправлены!");


        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }

    @Override
    public User getUserById(int id) {
        log.info(String.format("Получен запрос на отправку пользователя с id = %s", id));

        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);

        if (userRows.next()) {
            log.info(String.format("Пользователь с id = %s успешно отправлен клиенту", id));

            return new User(
                    userRows.getInt("user_id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate());

        }

        log.warn(String.format("Отсутствует пользователь с id = %s", id));

        throw new NoSuchElementException(String.format("Пользователь с id = %s отсутствует", id));
    }

    @Override
    public User createNewUser(User user) {
        log.info("Получен запрос на создание нового пользователя");

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            parameters.put("name", user.getLogin());
        } else {
            parameters.put("name", user.getName());
        }

        parameters.put("birthday", user.getBirthday());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        log.info("Пользователь {} успешно создан", user.getName());

        return getUserById(generatedId.intValue());
    }

    @Override
    public User updateUser(User user) {
        String name;
        log.info("Получен запрос на обновление пользователя");

        getUserById(user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            name = user.getLogin();
        } else {
            name = user.getName();
        }

        jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?," +
                        "birthday = ? WHERE user_id = ?", user.getEmail(), user.getLogin(), name,
                user.getBirthday(), user.getId());

        log.info("Пользователь с id = {} успешно обновлен", user.getId());

        return getUserById(user.getId());
    }

    @Override
    public User deleteUserById(int id) {
        log.info(String.format("Получен запрос на удаление пользователя с id = %s", id));

        User deletedUser = getUserById(id);

        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", id);

        log.info("Пользователь {} был успешно удален", deletedUser.getName());

        return deletedUser;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        log.info(String.format("Получен запрос на добавление в друзья. Пользователь с id = %s хочет добавить " +
                "пользователя с id = %s", userId, friendId));

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        jdbcTemplate.update("INSERT INTO user_friend (sender_id, recipients_id, friendship_status) VALUES (?, ?, ?)",
                user.getId(), friend.getId(), FriendshipStatus.FRIENDS.toString());

        jdbcTemplate.update("INSERT INTO user_friend (sender_id, recipients_id, friendship_status) VALUES (?, ?, ?)",
                friend.getId(), user.getId(), FriendshipStatus.IN_SUBSCRIBERS.toString());

        log.info(String.format("%s попал в список друзей пользователя %s", user.getName(), friend.getName()));
        log.info(String.format("%s попал в список подписчиков пользователя %s", friend.getName(), user.getName()));
        registerUserEvent(getAddFriendEvent(userId, friendId));
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        log.info(String.format("Получен запрос на удаление из друзей. Пользователь с id = %s хочет удалить друга " +
                "с id = %s", userId, friendId));

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        jdbcTemplate.update("DELETE FROM user_friend WHERE sender_id = ? AND recipients_id = ?", userId, friendId);

        log.info(String.format("Пользователи %s и %s больше не друзья", user.getName(), friend.getName()));
        registerUserEvent(getDeleteFriendEvent(userId, friendId));
    }

    @Override
    public List<User> getUsersFriends(int userId) {
        log.info(String.format("Получен запрос на отправку друзей пользователя с id = %s", userId));

        User user = getUserById(userId);

        log.info(String.format("Пользователю %s успешно отправлены его друзья", user.getName()));

        String sql = "SELECT *\n" +
                "FROM users AS u\n" +
                "JOIN user_friend AS uf ON u.user_id = uf.recipients_id\n" +
                "WHERE sender_id = ?\n" +
                "  AND friendship_status = 'FRIENDS'";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        log.info(String.format("Получен запрос на отправку общих друзей пользователей с id = %s и с id = %s ", userId,
                otherId));

        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        List<User> userFriend = getUsersFriends(user.getId());
        List<User> otherUserFriend = getUsersFriends(otherUser.getId());

        userFriend.retainAll(otherUserFriend);

        log.info(String.format("Список общих друзей пользователей %s и %s успешно отправлен", user.getName(),
                otherUser.getName()));

        return userFriend;
    }

    @Override
    public List<UserEvent> getUserFeed(int userId) {
        log.info(String.format("Получение ленты событий для пользователя с id = %s", userId));
        String sqlQuery = "SELECT event_id, user_id, event_type, operation, affected_entity_id, created_at " +
                "FROM user_events " +
                "WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapUserEvent(rs), userId);
//                "WHERE user_id in (%s)";
//        List<Integer> friendsIds = getUsersFriends(userId).stream().map(User::getId).collect(Collectors.toList());
//        String idPlaceholders = String.join(",", Collections.nCopies(friendsIds.size(), "?"));
//        return jdbcTemplate.query(String.format(sqlQuery, idPlaceholders),
//                friendsIds.toArray(),
//                (rs, rowNum) -> mapUserEvent(rs));
    }

    @Override
    public void registerUserEvent(UserEvent event) {
        String sqlQuery = "INSERT INTO user_events (user_id, event_type, operation, affected_entity_id) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                event.getUserId(),
                event.getEventType().toString(),
                event.getOperation().toString(),
                event.getEntityId());
        log.info("Событие записано");
    }

    private UserEvent mapUserEvent(ResultSet rs) throws SQLException {
        return new UserEvent(rs.getInt("event_id"),
                rs.getInt("user_id"),
                UserEvent.EventType.valueOf(rs.getString("event_type")),
                UserEvent.OperationType.valueOf(rs.getString("operation")),
                rs.getInt("affected_entity_id"),
                rs.getTimestamp("created_at", tzUTC).toInstant());
    }
}