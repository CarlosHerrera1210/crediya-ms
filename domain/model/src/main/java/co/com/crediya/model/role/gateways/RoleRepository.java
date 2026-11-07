package co.com.crediya.model.role.gateways;

import co.com.crediya.model.role.Role;

import java.util.Optional;

public interface RoleRepository {

    boolean existsById(Integer id);

    Optional<Role> findByName(String name);
}
