package org.example.oopproject1.controller;

import org.example.oopproject1.model.Application;
import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.example.oopproject1.security.JwtUtils;
import org.example.oopproject1.service.ApplicationService;
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

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ApplicationService applicationService;

    // mock out security
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/applications returns 200 and JSON list")
    void getAllApplications_returnsOk() throws Exception {
        Application a = new Application();
        a.setId("app1");
        when(applicationService.getAllApplications()).thenReturn(List.of(a));

        mvc.perform(get("/api/applications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("app1"));
    }
}
