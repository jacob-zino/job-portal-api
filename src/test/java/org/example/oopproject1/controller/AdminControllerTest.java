package org.example.oopproject1.controller;

import org.example.oopproject1.model.User;
import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.example.oopproject1.security.JwtUtils;
import org.example.oopproject1.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    // mock out security
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/admin/users returns 200 and JSON list")
    void getAllUsers_returnsOk() throws Exception {
        User u = new User();
        u.setId("123");
        u.setUsername("test");
        when(userService.getAllUsers()).thenReturn(List.of(u));

        mvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].username").value("test"));
    }
}
