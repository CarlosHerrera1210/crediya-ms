package co.com.crediya.api;

import co.com.crediya.api.exception.GlobalExceptionHandler;
import co.com.crediya.usecase.user.UserUseCase;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({UserController.class, GlobalExceptionHandler.class})
public class TestConfig {

    @Bean
    public UserUseCase userUseCase() {
        return Mockito.mock(UserUseCase.class);
    }
}
