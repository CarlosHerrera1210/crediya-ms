package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    List<User> findAll();

    Optional<User> findUserById(Long id);
}
