package co.com.crediya.model.exceptions;

public class UserNotFoundException extends BusinessException {

    public static final String CODE = "USER_NOT_FOUND";

    public UserNotFoundException(Long id) {
        super(CODE, "No existe un usuario con id " + id);
    }
}
