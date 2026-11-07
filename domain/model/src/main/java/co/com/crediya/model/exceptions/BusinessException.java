package co.com.crediya.model.exceptions;

import lombok.Getter;

/**
 * Base class for all business rule violations raised by the domain.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final String code;

    protected BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
