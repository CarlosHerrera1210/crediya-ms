package co.com.crediya.jpa;

import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.jpa.helper.AdapterOperations;
import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class JPARepositoryAdapter extends AdapterOperations<User, UserEntity, Long, JPARepository>
        implements UserRepository {

    private static final String DOCUMENT_UNIQUE_CONSTRAINT = "uk_usuarios_documento";
    private static final String ROLE_FOREIGN_KEY = "fk_usuarios_rol";

    public JPARepositoryAdapter(JPARepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    @Transactional
    public User save(User user) {
        log.debug("Persistiendo usuario con correo {}", user.getEmail());
        try {
            User saved = super.save(user);
            log.debug("Usuario persistido con id {}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            // A concurrent request passed the use case checks first; the database constraint rejected this one.
            log.warn("Violacion de integridad al persistir usuario con correo {}", user.getEmail());
            throw translateIntegrityViolation(e, user);
        }
    }

    private static RuntimeException translateIntegrityViolation(DataIntegrityViolationException e, User user) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage());
        if (detail.contains(DOCUMENT_UNIQUE_CONSTRAINT)) {
            return new DocumentAlreadyRegisteredException(user.getDocumentNumber());
        }
        if (detail.contains(ROLE_FOREIGN_KEY)) {
            return new ValidationException(List.of("El campo id_rol no corresponde a un rol existente"));
        }
        return new EmailAlreadyRegisteredException(user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }

    @Override
    protected UserEntity saveData(UserEntity data) {
        // Flush inside the transaction so constraint violations surface here and not at commit time.
        return repository.saveAndFlush(data);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return super.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return repository.findById(id).map(this::toEntity);
    }
}
