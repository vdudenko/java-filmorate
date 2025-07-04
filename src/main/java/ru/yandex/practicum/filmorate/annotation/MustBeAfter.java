package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import ru.yandex.practicum.filmorate.validator.AfterDateValidator;

@Documented
@Constraint(validatedBy = AfterDateValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MustBeAfter {

    String message() default "Date must be after {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value(); // Принимаем дату как строку формата "yyyy-MM-dd"
}
