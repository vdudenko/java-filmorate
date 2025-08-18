package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"email"})
@AllArgsConstructor
@Builder
public class User {
    private long id;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^\\S*$", message = "Поле не должно содержать пробелов")
    private String login;
    private String name;
    @NotNull
    @PastOrPresent
    private LocalDate birthday;
    @JsonIgnore
    private Set<Long> friends = new HashSet<>();

    public void addFriend(long friendId) {
        this.friends.add(friendId);
    }

    public void deleteFriend(long friendId) {
        this.friends.remove(friendId);
    }
}
