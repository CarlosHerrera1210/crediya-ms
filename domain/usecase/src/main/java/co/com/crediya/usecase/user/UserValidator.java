package co.com.crediya.usecase.user;

import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business validations applied to a user before it is registered.
 */
final class UserValidator {

    static final BigDecimal MIN_SALARY = BigDecimal.ZERO;
    static final BigDecimal MAX_SALARY = new BigDecimal("15000000");
    static final int MAX_DOCUMENT_LENGTH = 20;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private UserValidator() {
    }

    static void validate(User user) {
        List<String> errors = new ArrayList<>();

        if (isBlank(user.getNames())) {
            errors.add("El campo nombres es obligatorio");
        }
        if (isBlank(user.getLastNames())) {
            errors.add("El campo apellidos es obligatorio");
        }
        if (isBlank(user.getEmail())) {
            errors.add("El campo correo_electronico es obligatorio");
        } else if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.add("El campo correo_electronico no tiene un formato válido");
        }
        // documento_identidad is optional (not part of the HU01 required fields), validated only when present.
        if (!isBlank(user.getDocumentNumber()) && user.getDocumentNumber().length() > MAX_DOCUMENT_LENGTH) {
            errors.add("El campo documento_identidad no puede tener más de " + MAX_DOCUMENT_LENGTH + " caracteres");
        }
        if (user.getBaseSalary() == null) {
            errors.add("El campo salario_base es obligatorio");
        } else if (user.getBaseSalary().compareTo(MIN_SALARY) < 0
                || user.getBaseSalary().compareTo(MAX_SALARY) > 0) {
            errors.add("El campo salario_base debe estar entre 0 y 15000000");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
