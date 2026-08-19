package com.example.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.audit.api.dto.CreateUserRequest;
import com.example.audit.api.dto.UserResponse;
import com.example.audit.domain.User;
import com.example.audit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, new BCryptPasswordEncoder(4));
    }

    @Test
    void createUserHashesPasswordAndStoresAuditMetadata() {
        when(repository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.createUser(
            new CreateUserRequest("newuser", "password", "ROLE_AUDIT_READER", "description"),
            "admin");

        assertEquals("newuser", response.username());
        verify(repository).save(argThat(user ->
            user.getPasswordHash() != null
                && !"password".equals(user.getPasswordHash())
                && "admin".equals(user.getCreatedBy())
                && user.getActive()));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(repository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> service.createUser(
            new CreateUserRequest("existing", "password", "ROLE_AUDIT_READER", null), "admin"));
        verify(repository, never()).save(any());
    }

    @Test
    void createUserRejectsInvalidRole() {
        when(repository.findByUsername("newuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.createUser(
            new CreateUserRequest("newuser", "password", "ROLE_UNKNOWN", null), "admin"));
        verify(repository, never()).save(any());
    }

    @Test
    void findsAndListsUsers() {
        User user = new User("reader", "hash", "ROLE_AUDIT_READER");
        when(repository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByActiveTrue()).thenReturn(List.of(user));
        when(repository.findByRole("ROLE_AUDIT_READER")).thenReturn(List.of(user));

        assertEquals("reader", service.getUserByUsername("reader").orElseThrow().username());
        assertEquals("reader", service.getUserById(1L).orElseThrow().username());
        assertEquals(1, service.listActiveUsers().size());
        assertEquals(1, service.getUsersByRole("ROLE_AUDIT_READER").size());
    }

    @Test
    void updatesRole() {
        User user = new User("reader", "hash", "ROLE_AUDIT_READER");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        UserResponse response = service.updateUserRole(1L, "ROLE_ADMIN", "admin");

        assertEquals("ROLE_ADMIN", response.role());
    }

    @Test
    void rejectsInvalidRoleUpdate() {
        assertThrows(IllegalArgumentException.class, () -> service.updateUserRole(1L, "INVALID", "admin"));
        verify(repository, never()).findById(any());
    }

    @Test
    void deactivatesAndReactivatesUser() {
        User user = new User("reader", "hash", "ROLE_AUDIT_READER");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        assertFalse(service.deactivateUser(1L, "admin").active());
        assertTrue(service.reactivateUser(1L, "admin").active());
    }

    @Test
    void reportsActiveUser() {
        when(repository.findByUsernameAndActiveTrue("reader")).thenReturn(Optional.of(new User()));
        when(repository.findByUsernameAndActiveTrue("missing")).thenReturn(Optional.empty());

        assertTrue(service.isUserActive("reader"));
        assertFalse(service.isUserActive("missing"));
    }
}
