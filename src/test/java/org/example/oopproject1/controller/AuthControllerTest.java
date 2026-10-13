// src/test/java/org/example/oopproject1/controller/AuthControllerTest.java
package org.example.oopproject1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oopproject1.dto.LoginRequest;
import org.example.oopproject1.model.User;
import org.example.oopproject1.security.JwtUtils;
import org.example.oopproject1.service.RecruiterService;
import org.example.oopproject1.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private RecruiterService recruiterService;

    @MockBean
    private JwtUtils jwtUtils;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void authenticateUser_returnsJwt() throws Exception {
        // 1) Build login payload
        LoginRequest req = new LoginRequest();
        req.setUsername("u");
        req.setPassword("p");

        // 2) Stub authenticationManager.authenticate(...) -> return a mock Authentication
        Authentication auth = mock(Authentication.class);
        given(authenticationManager.authenticate(any())).willReturn(auth);

        // 3) Return your own model.User as the principal (so the cast succeeds)
        User principal = new User();
        principal.setId("42");
        principal.setUsername("u");
        principal.setEmail("u@example.com");
        principal.setRoles(Collections.emptyList());
        given(auth.getPrincipal()).willReturn(principal);

        // 4) Stub JWT generation
        given(jwtUtils.generateJwtToken(auth)).willReturn("token");

        // 5) Perform POST /api/auth/login and verify we get HTTP 200 + {"token":"token"}
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }
}
