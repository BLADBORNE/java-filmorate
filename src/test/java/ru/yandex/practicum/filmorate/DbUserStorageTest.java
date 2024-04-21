package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
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
        assertEquals(1,userService.getUsers().size());
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

        userService.createNewUser(newUser);

        User savedUser = userService.getUserById(1);

        assertNotNull(savedUser);
        assertEquals(1,userService.getUsers().size());
        assertTrue(userService.getUsers().contains(savedUser));

        newUser.setName("UpdateName");
        newUser.setLogin("New");

        userService.updateUser(newUser);

        assertEquals(1,userService.getUsers().size());
        assertTrue(userService.getUsers().contains(newUser));
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
        assertEquals(1,userService.getUsers().size());
        assertTrue(userService.getUsers().contains(savedUser));

        userService.deleteUserById(savedUser.getId());

        assertEquals(0,userService.getUsers().size());
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

        userService.createNewUser(user1);
        userService.createNewUser(user2);

        assertEquals(2, userService.getUsers().size());
        assertTrue(userService.getUsers().contains(user1));
        assertTrue(userService.getUsers().contains(user2));

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(userService.getUsersFriends(user1.getId()).contains(user2));
        assertFalse(userService.getUsersFriends(user2.getId()).contains(user1));
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

        userService.createNewUser(user1);
        userService.createNewUser(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(userService.getUsersFriends(user1.getId()).contains(user2));
        assertFalse(userService.getUsersFriends(user2.getId()).contains(user1));
        assertEquals(2, userService.getUsers().size());

        userService.deleteFriend(user1.getId(), user2.getId());

        assertTrue(userService.getUsers().contains(user1));
        assertTrue(userService.getUsers().contains(user2));

        assertFalse(userService.getUsersFriends(user1.getId()).contains(user2));
        assertFalse(userService.getUsersFriends(user2.getId()).contains(user1));
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