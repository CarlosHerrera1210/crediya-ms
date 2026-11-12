package co.com.crediya.usecase.user;

import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.role.Role;
import co.com.crediya.model.role.gateways.RoleRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class UserUseCase {

    private static final Logger LOG = Logger.getLogger(UserUseCase.class.getName());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User registerUser(User user) {
        User normalized = normalize(user);
        LOG.log(Level.INFO, "Iniciando registro de usuario con correo {0}", normalized.getEmail());

        UserValidator.validate(normalized);

        if (userRepository.existsByEmail(normalized.getEmail())) {
            LOG.log(Level.WARNING, "Registro rechazado: el correo {0} ya existe", normalized.getEmail());
            throw new EmailAlreadyRegisteredException(normalized.getEmail());
        }

        if (normalized.getDocumentNumber() != null
                && userRepository.existsByDocumentNumber(normalized.getDocumentNumber())) {
            LOG.log(Level.WARNING, "Registro rechazado: el documento {0} ya existe", normalized.getDocumentNumber());
            throw new DocumentAlreadyRegisteredException(normalized.getDocumentNumber());
        }

        User saved = userRepository.save(normalized.toBuilder().roleId(resolveRoleId(normalized.getRoleId())).build());
        LOG.log(Level.INFO, "Usuario registrado exitosamente con id {0}", saved.getId());
        return saved;
    }

    public List<User> listUsers() {
        List<User> users = userRepository.findAll();
        LOG.log(Level.INFO, "Consulta de usuarios: {0} encontrados", users.size());
        return users;
    }

    public User getUserById(Long id) {
        return userRepository.findUserById(id)
                .orElseThrow(() -> {
                    LOG.log(Level.WARNING, "Consulta rechazada: no existe usuario con id {0}", id);
                    return new UserNotFoundException(id);
                });
    }

    /**
     * Returns the requested role if it exists, or the default role when none was requested.
     */
    private Integer resolveRoleId(Integer requestedRoleId) {
        if (requestedRoleId == null) {
            return roleRepository.findByName(Role.DEFAULT_ROLE_NAME)
                    .map(Role::getId)
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe el rol por defecto " + Role.DEFAULT_ROLE_NAME + " en la base de datos"));
        }
        if (!roleRepository.existsById(requestedRoleId)) {
            LOG.log(Level.WARNING, "Registro rechazado: el rol {0} no existe", requestedRoleId);
            throw new ValidationException(List.of("El campo id_rol no corresponde a un rol existente"));
        }
        return requestedRoleId;
    }

    private static User normalize(User user) {
        return user.toBuilder()
                .id(null)
                .names(trim(user.getNames()))
                .lastNames(trim(user.getLastNames()))
                .address(trim(user.getAddress()))
                .phone(trim(user.getPhone()))
                .documentNumber(trimToNull(user.getDocumentNumber()))
                .email(user.getEmail() == null ? null : user.getEmail().trim().toLowerCase(Locale.ROOT))
                .build();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Blank optional values are stored as null so the unique constraint does not treat them as duplicates.
     */
    private static String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
