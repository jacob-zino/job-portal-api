// src/test/java/org/example/oopproject1/service/ApplicationServiceTest.java
package org.example.oopproject1.service;

import org.example.oopproject1.model.Application;
import org.example.oopproject1.repository.ApplicationRepository;
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
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Application sampleApp;

    @BeforeEach
    void setup() {
        sampleApp = new Application();
        sampleApp.setId("1");
    }

    @Test
    void getAllApplications_returnsList() {
        when(applicationRepository.findAll()).thenReturn(Arrays.asList(sampleApp));

        List<Application> result = applicationService.getAllApplications();

        assertEquals(1, result.size());
    }

    @Test
    void getPagedApplications_returnsPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Application> page = new PageImpl<>(Arrays.asList(sampleApp));
        when(applicationRepository.findAll(pageRequest)).thenReturn(page);

        Page<Application> result = applicationService.getAllApplications(pageRequest);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getApplicationById_found() {
        when(applicationRepository.findById("1")).thenReturn(Optional.of(sampleApp));

        Application result = applicationService.getApplicationById("1");

        assertEquals("1", result.getId());
    }

    @Test
    void getApplicationById_notFound() {
        when(applicationRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.getApplicationById("2"));
    }
}
