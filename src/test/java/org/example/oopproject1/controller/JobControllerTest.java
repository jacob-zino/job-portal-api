// src/test/java/org/example/oopproject1/controller/JobControllerTest.java
package org.example.oopproject1.controller;

import org.example.oopproject1.model.Job;
import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.example.oopproject1.service.JobService;
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

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)   // turn off Spring Security filters
class JobControllerTest {

    @Autowired
    private MockMvc mvc;

    // your real dependencies
    @MockBean
    private JobService jobService;

    @MockBean
    private UserService userService;

    // stub out the JWT filter so its JwtUtils dependency never gets wired
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/jobs returns 200 and JSON list")
    void getAllJobs_returnsOk() throws Exception {
        // arrange
        Job j = new Job();
        j.setId("job1");
        j.setTitle("My Test Job");
        when(jobService.getAllJobs()).thenReturn(List.of(j));

        // act + assert
        mvc.perform(get("/api/jobs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("job1"))
                .andExpect(jsonPath("$[0].title").value("My Test Job"));
    }
}
