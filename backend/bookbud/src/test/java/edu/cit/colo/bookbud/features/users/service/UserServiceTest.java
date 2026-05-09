package edu.cit.colo.bookbud.features.users.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.features.users.dto.UpdateUserRequest;
import edu.cit.colo.bookbud.features.users.dto.UserProfileDTO;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("test-user-id")
                .username("testuser")
                .passwordHash("encodedPassword")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetUserProfile_Success() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));

        // When
        UserProfileDTO result = userService.getUserProfile("test-user-id", "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById("test-user-id");
    }

    @Test
    void testGetUserProfile_NotFound() {
        // Given
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile("non-existent-id", "test-user-id");
        });
        verify(userRepository, times(1)).findById("non-existent-id");
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateUserRequest updateRequest = new UpdateUserRequest("updateduser", null, null, null);

        // When
        UserProfileDTO result = userService.updateUserProfile("test-user-id", "test-user-id", updateRequest);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById("test-user-id");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        // Given
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        UpdateUserRequest updateRequest = new UpdateUserRequest("testuser", null, null, null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile("non-existent-id", "non-existent-id", updateRequest);
        });
        verify(userRepository, times(1)).findById("non-existent-id");
        verify(userRepository, never()).save(any(User.class));
    }
}
