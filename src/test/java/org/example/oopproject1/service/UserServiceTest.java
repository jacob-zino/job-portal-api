// src/test/java/org/example/oopproject1/service/UserServiceTest.java
package org.example.oopproject1.service;

import org.example.oopproject1.model.User;
import org.example.oopproject1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User();
        sampleUser.setId("1");
        sampleUser.setUsername("test");
    }

    @Test
    void findByUsername_found() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByUsername("test");

        assertTrue(result.isPresent());
    }

    @Test
    void findByUsername_notFound() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("nope");

        assertFalse(result.isPresent());
    }

    @Test
    void registerUser_savesUser() {
        // arrange
        User newUser = new User();
        newUser.setUsername("u");
        newUser.setEmail("e");
        newUser.setPassword("p");
        newUser.setRoles(null);

        // stub the encoder so it doesn't NPE and returns the same password
        when(passwordEncoder.encode("p")).thenReturn("p");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // act
        User result = userService.registerUser("u", "e", "p", "USER");

        // assert
        assertEquals("u", result.getUsername());
        verify(userRepository).save(any(User.class));
    }
}
