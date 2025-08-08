package ru.yandex.practicum.filmorate;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
	private final UserDbStorage userDbStorage;
	private final FilmDbStorage filmDbStorage;
	private final LikeDbStorage likeDbStorage;
	private final FriendDbStorage friendDbStorage;
	private final JdbcTemplate jdbcTemplate;

	private User user1;
	private User user2;
	private Film film1;
	private Film film2;

	@BeforeEach
	void setUp() {
		// Очистка таблиц перед каждым тестом
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
		jdbcTemplate.execute("TRUNCATE TABLE user_friends");
		jdbcTemplate.execute("TRUNCATE TABLE film_likes");
		jdbcTemplate.execute("TRUNCATE TABLE film_genres");
		jdbcTemplate.execute("TRUNCATE TABLE films");
		jdbcTemplate.execute("TRUNCATE TABLE users");
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

		// Сброс счётчиков
		jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
		jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

		// Подготовка тестовых данных
		user1 = User.builder()
				.email("user1@yandex.ru")
				.login("user1login")
				.name("User One")
				.birthday(LocalDate.of(1990, 1, 1))
				.build();

		user2 = User.builder()
				.email("user2@yandex.ru")
				.login("user2login")
				.name("User Two")
				.birthday(LocalDate.of(1995, 5, 5))
				.build();

		film1 = Film.builder()
				.name("Film One")
				.description("Description of film one")
				.releaseDate(LocalDate.of(2020, 1, 1))
				.duration(120)
				.mpa(ru.yandex.practicum.filmorate.model.Rating.builder().id(1L).build())
				.build();

		film2 = Film.builder()
				.name("Film Two")
				.description("Description of film two")
				.releaseDate(LocalDate.of(2021, 1, 1))
				.duration(150)
				.mpa(ru.yandex.practicum.filmorate.model.Rating.builder().id(2L).build())
				.build();
	}

	@Test
	public void testCreateUserAndGetUserById() {
		User created = userDbStorage.create(user1);
		assertThat(created.getId()).isPositive();

		Optional<User> found = userDbStorage.findById(created.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo(user1.getEmail());
	}

	@Test
	public void testGetUsers() {
		userDbStorage.create(user1);
		userDbStorage.create(user2);

		List<User> users = (List<User>) userDbStorage.findAll();
		assertThat(users).hasSize(2);
		assertThat(users).extracting(User::getId).containsExactly(1L, 2L);
	}

	@Test
	public void testUpdateUser() {
		User created = userDbStorage.create(user1);
		created.setName("Updated Name");

		User updated = userDbStorage.update(created);
		assertThat(updated.getName()).isEqualTo("Updated Name");
	}

	@Test
	public void testCreateFilmAndGetFilmById() {
		Film created = filmDbStorage.create(film1);
		assertThat(created.getId()).isPositive();

		Optional<Film> found = filmDbStorage.findById(created.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo(film1.getName());
	}

	@Test
	public void testGetFilms() {
		filmDbStorage.create(film1);
		filmDbStorage.create(film2);

		List<Film> films = (List<Film>) filmDbStorage.findAll();
		assertThat(films).hasSize(2);
		assertThat(films).extracting(Film::getName).contains("Film One", "Film Two");
	}

	@Test
	public void testUpdateFilm() {
		Film created = filmDbStorage.create(film1);
		created.setName("Updated Film Name");

		Film updated = filmDbStorage.update(created);
		assertThat(updated.getName()).isEqualTo("Updated Film Name");
	}

	@Test
	public void testAddLike() {
		User user = userDbStorage.create(user1);
		Film film = filmDbStorage.create(film1);

		likeDbStorage.addLike(film.getId(), user.getId());

		// Проверим, что лайк добавлен
		List<?> likes = jdbcTemplate.queryForList(
				"SELECT * FROM film_likes WHERE film_id = ? AND user_id = ?",
				film.getId(), user.getId()
		);
		assertThat(likes).hasSize(1);
	}

	@Test
	public void testDeleteLike() {
		User user = userDbStorage.create(user1);
		Film film = filmDbStorage.create(film1);

		likeDbStorage.addLike(film.getId(), user.getId());
		likeDbStorage.deleteLike(film.getId(), user.getId());

		List<?> likes = jdbcTemplate.queryForList(
				"SELECT * FROM film_likes WHERE film_id = ? AND user_id = ?",
				film.getId(), user.getId()
		);
		assertThat(likes).isEmpty();
	}

	@Test
	public void testGetPopularFilms() {
		User user1 = userDbStorage.create(User.builder()
				.email("p1@yandex.ru").login("p1").birthday(LocalDate.now().minusYears(20)).build());
		User user2 = userDbStorage.create(User.builder()
				.email("p2@yandex.ru").login("p2").birthday(LocalDate.now().minusYears(20)).build());

		Film film1 = filmDbStorage.create(Film.builder()
				.name("Popular Film").description("desc")
				.releaseDate(LocalDate.now().minusYears(1))
				.duration(100)
				.mpa(ru.yandex.practicum.filmorate.model.Rating.builder().id(1L).build())
				.build());

		Film film2 = filmDbStorage.create(Film.builder()
				.name("Less Popular Film").description("desc")
				.releaseDate(LocalDate.now().minusYears(1))
				.duration(100)
				.mpa(ru.yandex.practicum.filmorate.model.Rating.builder().id(1L).build())
				.build());

		likeDbStorage.addLike(film1.getId(), user1.getId());
		likeDbStorage.addLike(film1.getId(), user2.getId());
		likeDbStorage.addLike(film2.getId(), user1.getId());

		List<Film> popular = (List<Film>) filmDbStorage.getPopularFilms(10);
		assertThat(popular).hasSize(2);
		assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
	}

	@Test
	public void testAddFriend() {
		User user1 = userDbStorage.create(this.user1);
		User user2 = userDbStorage.create(this.user2);

		friendDbStorage.addFriend(user1.getId(), user2.getId());

		List<User> friends = (List<User>) friendDbStorage.getFriends(user1.getId());
		assertThat(friends).hasSize(1);
		assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
	}

	@Test
	public void testDeleteFriend() {
		User user1 = userDbStorage.create(this.user1);
		User user2 = userDbStorage.create(this.user2);

		friendDbStorage.addFriend(user1.getId(), user2.getId());
		friendDbStorage.deleteFriend(user1.getId(), user2.getId());

		List<User> friends = (List<User>) friendDbStorage.getFriends(user1.getId());
		assertThat(friends).isEmpty();
	}

	@Test
	public void testGetFriends() {
		User user1 = userDbStorage.create(this.user1);
		User user2 = userDbStorage.create(this.user2);

		friendDbStorage.addFriend(user1.getId(), user2.getId());

		List<User> friends = (List<User>) friendDbStorage.getFriends(user1.getId());
		assertThat(friends).hasSize(1);
		assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
	}

	@Test
	public void testGetCommonFriends() {
		User user1 = userDbStorage.create(User.builder()
				.email("cf1@yandex.ru").login("cf1").birthday(LocalDate.now().minusYears(20)).build());
		User user2 = userDbStorage.create(User.builder()
				.email("cf2@yandex.ru").login("cf2").birthday(LocalDate.now().minusYears(20)).build());
		User common = userDbStorage.create(User.builder()
				.email("common@yandex.ru").login("common").birthday(LocalDate.now().minusYears(20)).build());

		friendDbStorage.addFriend(user1.getId(), common.getId());
		friendDbStorage.addFriend(user2.getId(), common.getId());

		List<User> commonFriends = (List<User>) friendDbStorage.getIntersectFriends(user1.getId(), user2.getId());
		assertThat(commonFriends).hasSize(1);
		assertThat(commonFriends.get(0).getId()).isEqualTo(common.getId());
	}
}
