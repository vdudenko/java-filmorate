package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import ru.yandex.practicum.filmorate.annotation.MustBeAfter;

public class AfterDateValidator implements ConstraintValidator<MustBeAfter, LocalDate> {

    private LocalDate referenceDate;

    @Override
    public void initialize(MustBeAfter constraintAnnotation) {
        try {
            this.referenceDate = LocalDate.parse(constraintAnnotation.value());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        }
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true; // или false — зависит от ваших правил

        return !value.isBefore(referenceDate);
    }
}
