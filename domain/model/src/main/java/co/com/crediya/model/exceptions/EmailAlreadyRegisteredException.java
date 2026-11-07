package co.com.crediya.model.exceptions;

public class EmailAlreadyRegisteredException extends BusinessException {

    public static final String CODE = "EMAIL_ALREADY_REGISTERED";

    public EmailAlreadyRegisteredException(String email) {
        super(CODE, "El correo electrónico " + email + " ya se encuentra registrado");
    }
}
