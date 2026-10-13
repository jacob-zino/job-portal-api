package org.example.oopproject1.controller;

import org.example.oopproject1.model.User;
import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.example.oopproject1.security.JwtUtils;
import org.example.oopproject1.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // mock out security
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getCurrentUser_returnsOk() throws Exception {
        // arrange
        User u = new User();
        u.setId("1");
        u.setUsername("u");
        u.setEmail("e");
        when(userService.findByUsername("u")).thenReturn(Optional.of(u));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u");

        // act + assert
        mockMvc.perform(get("/api/users/me")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("e"));
    }
}
