// src/test/java/org/example/oopproject1/service/RecruiterServiceTest.java
package org.example.oopproject1.service;

import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.repository.RecruiterRepository;
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
class RecruiterServiceTest {

    @Mock
    private RecruiterRepository recruiterRepository;

    @InjectMocks
    private RecruiterService recruiterService;

    private Recruiter sampleRec;

    @BeforeEach
    void setup() {
        sampleRec = new Recruiter();
        sampleRec.setId("1");
        sampleRec.setEmail("rec@example.com");
    }

    @Test
    void getAllRecruiters_returnsList() {
        when(recruiterRepository.findAll()).thenReturn(Arrays.asList(sampleRec));

        List<Recruiter> result = recruiterService.getAllRecruiters();

        assertEquals(1, result.size());
    }

    @Test
    void getPagedRecruiters_returnsPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Recruiter> page = new PageImpl<>(Arrays.asList(sampleRec));
        when(recruiterRepository.findAll(pageRequest)).thenReturn(page);

        Page<Recruiter> result = recruiterService.getAllRecruiters(pageRequest);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRecruiterById_found() {
        when(recruiterRepository.findById("1")).thenReturn(Optional.of(sampleRec));

        Recruiter result = recruiterService.getRecruiterById("1");

        assertEquals("rec@example.com", result.getEmail());
    }

    @Test
    void getRecruiterById_notFound() {
        when(recruiterRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> recruiterService.getRecruiterById("2"));
    }
}
