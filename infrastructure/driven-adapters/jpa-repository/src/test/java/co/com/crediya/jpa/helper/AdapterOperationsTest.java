package co.com.crediya.jpa.helper;

import co.com.crediya.jpa.JPARepository;
import co.com.crediya.jpa.JPARepositoryAdapter;
import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdapterOperationsTest {

    private JPARepository repository;
    private JPARepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(JPARepository.class);
        adapter = new JPARepositoryAdapter(repository, new ObjectMapperImp());
    }

    private static User user() {
        return User.builder()
                .names("Ana")
                .lastNames("Perez")
                .birthDate(LocalDate.of(1990, 5, 20))
                .address("Calle 1")
                .phone("300")
                .email("ana@mail.com")
                .baseSalary(new BigDecimal("1000"))
                .build();
    }

    private static UserEntity entity(Long id) {
        return UserEntity.builder()
                .id(id)
                .names("Ana")
                .lastNames("Perez")
                .birthDate(LocalDate.of(1990, 5, 20))
                .address("Calle 1")
                .phone("300")
                .email("ana@mail.com")
                .baseSalary(new BigDecimal("1000"))
                .build();
    }

    @Test
    void shouldMapAndSaveUser() {
        when(repository.saveAndFlush(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity data = inv.getArgument(0);
            assertNull(data.getId());
            assertEquals("ana@mail.com", data.getEmail());
            assertEquals(LocalDate.of(1990, 5, 20), data.getBirthDate());
            return data.toBuilder().id(1L).build();
        });

        User result = adapter.save(user());

        assertEquals(1L, result.getId());
        assertEquals("Ana", result.getNames());
        assertEquals("Perez", result.getLastNames());
        assertEquals(LocalDate.of(1990, 5, 20), result.getBirthDate());
        assertEquals(new BigDecimal("1000"), result.getBaseSalary());
    }

    @Test
    void shouldTranslateUniqueConstraintViolation() {
        when(repository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("uk_usuarios_correo"));

        User input = user();
        assertThrows(EmailAlreadyRegisteredException.class, () -> adapter.save(input));
    }

    @Test
    void shouldTranslateDocumentUniqueConstraintViolation() {
        when(repository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key violates uk_usuarios_documento"));

        User input = user();
        assertThrows(DocumentAlreadyRegisteredException.class, () -> adapter.save(input));
    }

    @Test
    void shouldTranslateRoleForeignKeyViolation() {
        when(repository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("insert violates fk_usuarios_rol"));

        User input = user();
        assertThrows(ValidationException.class, () -> adapter.save(input));
    }

    @Test
    void shouldMapDocumentAndRoleWhenSaving() {
        when(repository.saveAndFlush(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity data = inv.getArgument(0);
            assertEquals("1020304050", data.getDocumentNumber());
            assertEquals(3, data.getRoleId());
            return data.toBuilder().id(1L).build();
        });

        User result = adapter.save(user().toBuilder().documentNumber("1020304050").roleId(3).build());

        assertEquals("1020304050", result.getDocumentNumber());
        assertEquals(3, result.getRoleId());
    }

    @Test
    void shouldDelegateExistsByDocumentNumber() {
        when(repository.existsByDocumentNumber("1020304050")).thenReturn(true);

        assertTrue(adapter.existsByDocumentNumber("1020304050"));
    }

    @Test
    void shouldFindUserById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L)));

        assertEquals(1L, adapter.findUserById(1L).orElseThrow().getId());
        assertTrue(adapter.findUserById(999L).isEmpty());
    }

    @Test
    void shouldDelegateExistsByEmail() {
        when(repository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertTrue(adapter.existsByEmail("ana@mail.com"));
    }

    @Test
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L)));

        assertEquals(1L, adapter.findById(1L).getId());
        assertNull(adapter.findById(999L));
    }

    @Test
    void shouldFindAllAndByExample() {
        when(repository.findAll()).thenReturn(List.of(entity(1L), entity(2L)));
        when(repository.findAll(any(Example.class))).thenReturn(List.of(entity(1L)));

        assertEquals(2, adapter.findAll().size());
        assertEquals(1, adapter.findByExample(user()).size());
    }
}
