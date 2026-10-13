// src/test/java/org/example/oopproject1/service/JobServiceTest.java
package org.example.oopproject1.service;

import org.example.oopproject1.model.Job;
import org.example.oopproject1.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job sampleJob;

    @BeforeEach
    void setup() {
        sampleJob = new Job();
        sampleJob.setId("1");
        sampleJob.setTitle("Test Job");
    }

    @Test
    void getAllJobs_returnsList() {
        when(jobRepository.findAll()).thenReturn(Arrays.asList(sampleJob));

        List<Job> result = jobService.getAllJobs();

        assertEquals(1, result.size());
        verify(jobRepository).findAll();
    }

    @Test
    void getPagedJobs_returnsPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Job> page = new PageImpl<>(Arrays.asList(sampleJob));
        when(jobRepository.findAll(pageRequest)).thenReturn(page);

        Page<Job> result = jobService.getAllJobs(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).findAll(pageRequest);
    }

    @Test
    void getJobById_found() {
        when(jobRepository.findById("1")).thenReturn(Optional.of(sampleJob));

        Job result = jobService.getJobById("1");

        assertEquals("Test Job", result.getTitle());
    }

    @Test
    void getJobById_notFound() {
        when(jobRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> jobService.getJobById("2"));
    }
}
