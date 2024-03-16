package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void createUserService() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    public void shouldMakeUser1AndUser2Friends() {
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

        User u1 = userService.createNewUser(user1);
        User u2 = userService.createNewUser(user2);

        assertEquals(2, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(user1));
        assertTrue(userService.getUsers().contains(user2));

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(u1.getUserFriends().contains(u2.getId()));
        assertTrue(u2.getUserFriends().contains(u1.getId()));
    }

    @Test
    public void shouldMakeUser1AndUser2Unfriends() {
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

        User u1 = userService.createNewUser(user1);
        User u2 = userService.createNewUser(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(u1.getUserFriends().contains(u2.getId()));
        assertTrue(u2.getUserFriends().contains(u1.getId()));

        userService.deleteFriend(user1.getId(), user2.getId());

        assertEquals(2, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(user1));
        assertTrue(userService.getUsers().contains(user2));
        assertFalse(u1.getUserFriends().contains(u2.getId()));
        assertFalse(u2.getUserFriends().contains(u1.getId()));
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

        userService.createNewUser(user1);
        userService.createNewUser(user2);
        userService.createNewUser(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> userFriends = userService.getUsersFriends(user1.getId());

        assertNotNull(userFriends);
        assertEquals(2, userFriends.size());
        assertTrue(userFriends.contains(user2));
        assertTrue(userFriends.contains(user3));
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

        userService.createNewUser(user1);
        userService.createNewUser(user2);
        userService.createNewUser(user3);

        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        List<User> commonUserFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertNotNull(commonUserFriends);
        assertEquals(1, commonUserFriends.size());
        assertTrue(commonUserFriends.contains(user3));
    }
}