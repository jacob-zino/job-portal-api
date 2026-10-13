// src/test/java/org/example/oopproject1/controller/RecruiterControllerTest.java
package org.example.oopproject1.controller;

import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.example.oopproject1.service.ApplicationService;
import org.example.oopproject1.service.JobService;
import org.example.oopproject1.service.RecruiterService;
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

@WebMvcTest(RecruiterController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecruiterControllerTest {

    @Autowired
    private MockMvc mvc;

    // all four services from the RecruiterController constructor:
    @MockBean
    private RecruiterService recruiterService;
    @MockBean
    private UserService userService;
    @MockBean
    private JobService jobService;
    @MockBean
    private ApplicationService applicationService;

    // stub out the security filter so JwtUtils (etc.) never needs to be wired
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/recruiters returns 200 and JSON list")
    void getAllRecruiters_returnsOk() throws Exception {
        Recruiter r = new Recruiter();
        r.setId("rec1");

        when(recruiterService.getAllRecruiters()).thenReturn(List.of(r));

        mvc.perform(get("/api/recruiters")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("rec1"));
    }
}
