package co.com.crediya.jpa;

import co.com.crediya.jpa.entity.RoleEntity;
import co.com.crediya.model.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleRepositoryAdapterTest {

    private RoleJPARepository repository;
    private RoleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(RoleJPARepository.class);
        adapter = new RoleRepositoryAdapter(repository);
    }

    @Test
    void shouldDelegateExistsById() {
        when(repository.existsById(1)).thenReturn(true);

        assertTrue(adapter.existsById(1));
        assertFalse(adapter.existsById(99));
    }

    @Test
    void shouldMapRoleFoundByName() {
        when(repository.findByName("CLIENTE")).thenReturn(Optional.of(
                RoleEntity.builder().id(3).name("CLIENTE").description("Cliente").build()));

        Role role = adapter.findByName("CLIENTE").orElseThrow();

        assertEquals(3, role.getId());
        assertEquals("CLIENTE", role.getName());
        assertEquals("Cliente", role.getDescription());
    }

    @Test
    void shouldReturnEmptyWhenRoleNameDoesNotExist() {
        when(repository.findByName("NO_EXISTE")).thenReturn(Optional.empty());

        assertTrue(adapter.findByName("NO_EXISTE").isEmpty());
    }
}
