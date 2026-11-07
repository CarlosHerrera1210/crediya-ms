package co.com.crediya.api;

import co.com.crediya.api.dto.UserRequest;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest request) {
        log.info("POST /api/v1/usuarios - solicitud de registro recibida");
        User created = userUseCase.registerUser(UserDtoMapper.toDomain(request));
        log.info("POST /api/v1/usuarios - usuario creado con id {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDtoMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        log.info("GET /api/v1/usuarios - consulta de todos los usuarios");
        List<UserResponse> users = userUseCase.listUsers().stream()
                .map(UserDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        log.info("GET /api/v1/usuarios/{} - consulta de usuario por id", id);
        return ResponseEntity.ok(UserDtoMapper.toResponse(userUseCase.getUserById(id)));
    }
}
