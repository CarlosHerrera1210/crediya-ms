package co.com.crediya.api;

import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestConfig.class)
class UserControllerTest {

    private static final String URI = "/api/v1/usuarios";
    private static final String VALID_BODY = """
            {
              "nombres": "Ana",
              "apellidos": "Perez",
              "fecha_nacimiento": "1990-05-20",
              "direccion": "Calle 1",
              "telefono": "3001234567",
              "correo_electronico": "ana@mail.com",
              "salario_base": 2500000,
              "documento_identidad": "1020304050",
              "id_rol": 2
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserUseCase userUseCase;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        Mockito.reset(userUseCase);
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldCreateUser() {
        when(userUseCase.registerUser(any(User.class))).thenAnswer(inv ->
                inv.<User>getArgument(0).toBuilder().id(1L).build());

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.nombres").isEqualTo("Ana")
                .jsonPath("$.apellidos").isEqualTo("Perez")
                .jsonPath("$.fecha_nacimiento").isEqualTo("1990-05-20")
                .jsonPath("$.correo_electronico").isEqualTo("ana@mail.com")
                .jsonPath("$.salario_base").isEqualTo(2500000)
                .jsonPath("$.documento_identidad").isEqualTo("1020304050")
                .jsonPath("$.id_rol").isEqualTo(2);

        Mockito.verify(userUseCase).registerUser(Mockito.argThat(u ->
                "Ana".equals(u.getNames())
                        && LocalDate.of(1990, 5, 20).equals(u.getBirthDate())
                        && new BigDecimal("2500000").compareTo(u.getBaseSalary()) == 0
                        && "1020304050".equals(u.getDocumentNumber())
                        && Integer.valueOf(2).equals(u.getRoleId())));
    }

    @Test
    void shouldSendNullRoleWhenRequestOmitsIt() {
        when(userUseCase.registerUser(any(User.class))).thenAnswer(inv ->
                inv.<User>getArgument(0).toBuilder().id(1L).roleId(3).build());

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY.replace(",\n  \"id_rol\": 2", ""))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id_rol").isEqualTo(3);

        Mockito.verify(userUseCase).registerUser(Mockito.argThat(u -> u.getRoleId() == null));
    }

    @Test
    void shouldReturnConflictWhenDocumentExists() {
        when(userUseCase.registerUser(any(User.class)))
                .thenThrow(new DocumentAlreadyRegisteredException("1020304050"));

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("DOCUMENT_ALREADY_REGISTERED");
    }

    @Test
    void shouldReturnBadRequestOnValidationError() {
        when(userUseCase.registerUser(any(User.class)))
                .thenThrow(new ValidationException(List.of("El campo nombres es obligatorio")));

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors[0]").isEqualTo("El campo nombres es obligatorio")
                .jsonPath("$.path").isEqualTo(URI);
    }

    @Test
    void shouldReturnConflictWhenEmailExists() {
        when(userUseCase.registerUser(any(User.class)))
                .thenThrow(new EmailAlreadyRegisteredException("ana@mail.com"));

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("EMAIL_ALREADY_REGISTERED");
    }

    @Test
    void shouldReturnBadRequestWhenSalaryIsNotNumeric() {
        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY.replace("2500000", "\"mucho\""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST_BODY");
    }

    @Test
    void shouldReturnBadRequestOnMalformedJson() {
        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ esto no es json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST_BODY");
    }

    @Test
    void shouldHideUnexpectedErrors() {
        when(userUseCase.registerUser(any(User.class))).thenThrow(new IllegalStateException("db password=secret"));

        client.post().uri(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(VALID_BODY)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INTERNAL_ERROR")
                .jsonPath("$.message").isEqualTo("Ocurrió un error inesperado, por favor intente más tarde");
    }

    @Test
    void shouldReturnUnsupportedMediaTypeWithControlledBody() {
        client.post().uri(URI)
                .contentType(MediaType.TEXT_PLAIN)
                .body("hola")
                .exchange()
                .expectStatus().isEqualTo(415)
                .expectBody()
                .jsonPath("$.code").isEqualTo("REQUEST_ERROR");
    }

    @Test
    void shouldReturnMethodNotAllowedWithControlledBody() {
        client.delete().uri(URI)
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.code").isEqualTo("REQUEST_ERROR");
    }

    private static User storedUser(Long id, String names, String email) {
        return User.builder()
                .id(id)
                .names(names)
                .lastNames("Perez")
                .birthDate(LocalDate.of(1990, 5, 20))
                .email(email)
                .baseSalary(new BigDecimal("2500000"))
                .build();
    }

    @Test
    void shouldListAllUsers() {
        when(userUseCase.listUsers()).thenReturn(List.of(
                storedUser(1L, "Ana", "ana@mail.com"),
                storedUser(2L, "Luis", "luis@mail.com")));

        client.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].correo_electronico").isEqualTo("ana@mail.com")
                .jsonPath("$[1].nombres").isEqualTo("Luis");
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoUsers() {
        when(userUseCase.listUsers()).thenReturn(List.of());

        client.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldGetUserById() {
        when(userUseCase.getUserById(1L)).thenReturn(storedUser(1L, "Ana", "ana@mail.com"));

        client.get().uri(URI + "/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.nombres").isEqualTo("Ana")
                .jsonPath("$.fecha_nacimiento").isEqualTo("1990-05-20")
                .jsonPath("$.salario_base").isEqualTo(2500000);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(userUseCase.getUserById(999L)).thenThrow(new UserNotFoundException(999L));

        client.get().uri(URI + "/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("No existe un usuario con id 999")
                .jsonPath("$.path").isEqualTo(URI + "/999");
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNotNumeric() {
        client.get().uri(URI + "/abc")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_PARAMETER")
                .jsonPath("$.message").isEqualTo("El parámetro id tiene un formato inválido");
    }
}
