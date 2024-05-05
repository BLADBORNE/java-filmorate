package ru.yandex.practicum.filmorate.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DbUserStorageTest {
    private final UserService userService;

    @Test
    public void shouldCreateUserAndFindUserById() {
        User newUser = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userService.createNewUser(newUser);

        User savedUser = userService.getUserById(1);

        assertNotNull(savedUser);
        assertEquals(1, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(savedUser));
    }

    @Test
    public void shouldUpdateAndFindUserById() {
        User newUser = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User createdUser = userService.createNewUser(newUser);

        User savedUser = userService.getUserById(createdUser.getId());

        assertNotNull(savedUser);
        assertEquals(1, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(savedUser));

        createdUser.setName("UpdateName");
        createdUser.setLogin("New");

        userService.updateUser(createdUser);

        assertEquals(1, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(createdUser));
        assertFalse(userService.getUsers().contains(savedUser));
    }

    @Test
    public void shouldDeleteFilmById() {
        User newUser = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userService.createNewUser(newUser);

        User savedUser = userService.getUserById(1);

        assertNotNull(savedUser);
        assertEquals(1, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(savedUser));

        userService.deleteUserById(savedUser.getId());

        assertEquals(0, userService.getUsers().size());
        assertFalse(userService.getUsers().contains(savedUser));
    }

    @Test
    public void shouldAddUser2ToUsers1FriendsListAndUser2ShouldNotHasUser1InHisFriendsList() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User user2 = User.builder()
                .email("iliashacool@gmail.com")
                .login("Maxim")
                .name("Max228")
                .birthday(LocalDate.of(2012, 12, 1))
                .build();

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);

        assertEquals(2, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(createdUser1));
        assertTrue(userService.getUsers().contains(createdUser2));

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        assertTrue(userService.getUsersFriends(createdUser1.getId()).contains(createdUser2));
        assertFalse(userService.getUsersFriends(createdUser2.getId()).contains(createdUser1));
    }

    @Test
    public void shouldDeleteUser2FromUser1FriendsListAndUser2ShouldNotHasUser1InHisFriendsList() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User user2 = User.builder()
                .email("iliashacool@gmail.com")
                .login("Maxim")
                .name("Max228")
                .birthday(LocalDate.of(2012, 12, 1))
                .build();

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        assertTrue(userService.getUsersFriends(createdUser1.getId()).contains(createdUser2));
        assertFalse(userService.getUsersFriends(createdUser2.getId()).contains(createdUser1));
        assertEquals(2, userService.getUsers().size());

        userService.deleteFriend(createdUser1.getId(), createdUser2.getId());

        assertTrue(userService.getUsers().contains(createdUser1));
        assertTrue(userService.getUsers().contains(createdUser2));

        assertFalse(userService.getUsersFriends(createdUser1.getId()).contains(createdUser2));
        assertFalse(userService.getUsersFriends(createdUser2.getId()).contains(createdUser1));
    }

    @Test
    public void shouldGetUsersFriends() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User user2 = User.builder()
                .email("iliashacool@gmail.com")
                .login("Maxim")
                .name("Max228")
                .birthday(LocalDate.of(2012, 12, 1))
                .build();

        User user3 = User.builder()
                .email("test12@gmail.com")
                .login("Anstasya")
                .name("Milo23")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();

        User created1 = userService.createNewUser(user1);
        User created2 = userService.createNewUser(user2);
        User created3 = userService.createNewUser(user3);

        userService.addFriend(created1.getId(), created2.getId());
        userService.addFriend(created1.getId(), created3.getId());

        List<User> userFriends = userService.getUsersFriends(created1.getId());

        assertNotNull(userFriends);
        assertEquals(2, userFriends.size());
        assertTrue(userFriends.contains(created2));
        assertTrue(userFriends.contains(created3));
    }

    @Test
    public void shouldGetCommonFriendsUser1AndUser2() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User user2 = User.builder()
                .email("iliashacool@gmail.com")
                .login("Maxim")
                .name("Max228")
                .birthday(LocalDate.of(2012, 12, 1))
                .build();

        User user3 = User.builder()
                .email("test12@gmail.com")
                .login("Anstasya")
                .name("Milo23")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();

        User created1 = userService.createNewUser(user1);
        User created2 = userService.createNewUser(user2);
        User created3 = userService.createNewUser(user3);

        userService.addFriend(created1.getId(), created3.getId());
        userService.addFriend(created2.getId(), created3.getId());

        List<User> commonUserFriends = userService.getCommonFriends(created1.getId(), created2.getId());

        assertNotNull(commonUserFriends);
        assertEquals(1, commonUserFriends.size());
        assertTrue(commonUserFriends.contains(created3));
    }

    @Test
    public void shouldRegisterUserFriendRequest() {
        User user1 = User.builder()
                .email("test1@gmail.com")
                .login("test1")
                .name("test1")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();
        User user2 = User.builder()
                .email("test2@gmail.com")
                .login("test2")
                .name("test2")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();
        user1 = userService.createNewUser(user1);
        user2 = userService.createNewUser(user2);
        userService.addFriend(user1.getId(), user2.getId());

        UserEvent event = userService.getUserFeed(user1.getId()).get(0);

        assertEquals(user1.getId(), event.getUserId());
        assertEquals(UserEvent.EventType.FRIEND, event.getEventType());
        assertEquals(UserEvent.OperationType.ADD, event.getOperation());
        assertEquals(user2.getId(), event.getEntityId());
    }

    @Test
    public void shouldRegisterUserDeleteFriendRequest() {
        User user1 = User.builder()
                .email("test1@gmail.com")
                .login("test1")
                .name("test1")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();
        User user2 = User.builder()
                .email("test2@gmail.com")
                .login("test2")
                .name("test2")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();
        user1 = userService.createNewUser(user1);
        user2 = userService.createNewUser(user2);
        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user1.getId(), user2.getId());

        UserEvent event = userService.getUserFeed(user1.getId()).get(1);

        assertEquals(user1.getId(), event.getUserId());
        assertEquals(UserEvent.EventType.FRIEND, event.getEventType());
        assertEquals(UserEvent.OperationType.REMOVE, event.getOperation());
        assertEquals(user2.getId(), event.getEntityId());
    }

}