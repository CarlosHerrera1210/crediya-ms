package co.com.crediya.model.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends BusinessException {

    public static final String CODE = "VALIDATION_ERROR";

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(CODE, "Los datos enviados no son válidos");
        this.errors = List.copyOf(errors);
    }
}
