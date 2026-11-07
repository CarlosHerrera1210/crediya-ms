package co.com.crediya.model.exceptions;

public class DocumentAlreadyRegisteredException extends BusinessException {

    public static final String CODE = "DOCUMENT_ALREADY_REGISTERED";

    public DocumentAlreadyRegisteredException(String documentNumber) {
        super(CODE, "El documento de identidad " + documentNumber + " ya se encuentra registrado");
    }
}
