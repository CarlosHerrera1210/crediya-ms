package co.com.crediya.usecase.user;

import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.role.Role;
import co.com.crediya.model.role.gateways.RoleRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserUseCaseTest {

    private static final int CLIENT_ROLE_ID = 3;

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userUseCase = new UserUseCase(userRepository, roleRepository);
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.<User>getArgument(0).toBuilder().id(100L).build());
        when(roleRepository.findByName(Role.DEFAULT_ROLE_NAME))
                .thenReturn(Optional.of(Role.builder().id(CLIENT_ROLE_ID).name(Role.DEFAULT_ROLE_NAME).build()));
    }

    private static User validUser() {
        return User.builder()
                .names("Ana Maria")
                .lastNames("Perez Gomez")
                .birthDate(LocalDate.of(1990, 5, 20))
                .address("Calle 1 # 2-3")
                .phone("3001234567")
                .email("ana.perez@mail.com")
                .baseSalary(new BigDecimal("2500000"))
                .documentNumber("1020304050")
                .build();
    }

    @Test
    void shouldRegisterValidUser() {
        when(userRepository.existsByEmail("ana.perez@mail.com")).thenReturn(false);

        User result = userUseCase.registerUser(validUser());

        assertEquals(100L, result.getId());
        assertEquals("ana.perez@mail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNormalizeDataAndIgnoreClientId() {
        User input = validUser().toBuilder()
                .id(55L)
                .names("  Ana  ")
                .email("  Ana.Perez@MAIL.com ")
                .build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        userUseCase.registerUser(input);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).existsByEmail("ana.perez@mail.com");
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("Ana", captor.getValue().getNames());
        assertEquals("ana.perez@mail.com", captor.getValue().getEmail());
    }

    @Test
    void shouldRejectWhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("ana.perez@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> userUseCase.registerUser(validUser()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReportAllMissingRequiredFields() {
        User input = User.builder().names(" ").lastNames(null).email("").baseSalary(null).build();

        ValidationException ex = assertThrows(ValidationException.class, () -> userUseCase.registerUser(input));

        assertEquals(5, ex.getErrors().size());
        assertTrue(ex.getErrors().contains("El campo nombres es obligatorio"));
        assertTrue(ex.getErrors().contains("El campo apellidos es obligatorio"));
        assertTrue(ex.getErrors().contains("El campo correo_electronico es obligatorio"));
        assertTrue(ex.getErrors().contains("El campo documento_identidad es obligatorio"));
        assertTrue(ex.getErrors().contains("El campo salario_base es obligatorio"));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"correo", "correo@", "@mail.com", "a@b", "a b@mail.com", "a@mail.c"})
    void shouldRejectInvalidEmailFormat(String email) {
        User input = validUser().toBuilder().email(email).build();

        ValidationException ex = assertThrows(ValidationException.class, () -> userUseCase.registerUser(input));

        assertEquals("El campo correo_electronico no tiene un formato válido", ex.getErrors().getFirst());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "15000000.01", "20000000"})
    void shouldRejectSalaryOutOfRange(String salary) {
        User input = validUser().toBuilder().baseSalary(new BigDecimal(salary)).build();

        ValidationException ex = assertThrows(ValidationException.class, () -> userUseCase.registerUser(input));

        assertEquals("El campo salario_base debe estar entre 0 y 15000000", ex.getErrors().getFirst());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "15000000", "15000000.00"})
    void shouldAcceptSalaryAtRangeLimits(String salary) {
        User input = validUser().toBuilder().baseSalary(new BigDecimal(salary)).build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        User result = userUseCase.registerUser(input);

        assertEquals(0, new BigDecimal(salary).compareTo(result.getBaseSalary()));
    }

    @Test
    void shouldAssignDefaultRoleWhenNoneIsRequested() {
        User result = userUseCase.registerUser(validUser());

        assertEquals(CLIENT_ROLE_ID, result.getRoleId());
        verify(roleRepository, never()).existsById(any());
    }

    @Test
    void shouldKeepRequestedRoleWhenItExists() {
        when(roleRepository.existsById(1)).thenReturn(true);

        User result = userUseCase.registerUser(validUser().toBuilder().roleId(1).build());

        assertEquals(1, result.getRoleId());
        verify(roleRepository, never()).findByName(anyString());
    }

    @Test
    void shouldRejectRequestedRoleThatDoesNotExist() {
        when(roleRepository.existsById(99)).thenReturn(false);
        User input = validUser().toBuilder().roleId(99).build();

        ValidationException ex = assertThrows(ValidationException.class, () -> userUseCase.registerUser(input));

        assertEquals("El campo id_rol no corresponde a un rol existente", ex.getErrors().getFirst());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenDefaultRoleIsMissingInDatabase() {
        when(roleRepository.findByName(Role.DEFAULT_ROLE_NAME)).thenReturn(Optional.empty());
        User input = validUser();

        assertThrows(IllegalStateException.class, () -> userUseCase.registerUser(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenDocumentAlreadyRegistered() {
        when(userRepository.existsByDocumentNumber("1020304050")).thenReturn(true);
        User input = validUser();

        DocumentAlreadyRegisteredException ex = assertThrows(DocumentAlreadyRegisteredException.class,
                () -> userUseCase.registerUser(input));

        assertEquals(DocumentAlreadyRegisteredException.CODE, ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldTrimDocumentNumber() {
        userUseCase.registerUser(validUser().toBuilder().documentNumber("  1020304050 ").build());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).existsByDocumentNumber("1020304050");
        verify(userRepository).save(captor.capture());
        assertEquals("1020304050", captor.getValue().getDocumentNumber());
    }

    @Test
    void shouldRejectDocumentLongerThanLimit() {
        User input = validUser().toBuilder().documentNumber("1".repeat(21)).build();

        ValidationException ex = assertThrows(ValidationException.class, () -> userUseCase.registerUser(input));

        assertEquals("El campo documento_identidad no puede tener más de 20 caracteres", ex.getErrors().getFirst());
    }

    @Test
    void shouldListAllUsers() {
        List<User> stored = List.of(
                validUser().toBuilder().id(1L).build(),
                validUser().toBuilder().id(2L).email("otro@mail.com").build());
        when(userRepository.findAll()).thenReturn(stored);

        List<User> result = userUseCase.listUsers();

        assertEquals(stored, result);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertTrue(userUseCase.listUsers().isEmpty());
    }

    @Test
    void shouldGetUserById() {
        User stored = validUser().toBuilder().id(1L).build();
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(stored));

        assertEquals(stored, userUseCase.getUserById(1L));
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findUserById(999L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userUseCase.getUserById(999L));

        assertEquals(UserNotFoundException.CODE, ex.getCode());
        assertEquals("No existe un usuario con id 999", ex.getMessage());
    }

    @Test
    void shouldAllowOptionalFieldsToBeNull() {
        User input = validUser().toBuilder().birthDate(null).address(null).phone(null).build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        User result = userUseCase.registerUser(input);

        assertEquals(100L, result.getId());
    }
}
