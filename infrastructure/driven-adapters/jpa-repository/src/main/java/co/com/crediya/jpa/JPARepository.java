package co.com.crediya.jpa;

import co.com.crediya.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPARepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);
}
