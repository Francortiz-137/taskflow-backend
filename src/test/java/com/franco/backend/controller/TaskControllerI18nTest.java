package com.franco.backend.controller;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.CorsProperties;
import com.franco.backend.config.I18nConfig;
import com.franco.backend.config.JwtProperties;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.security.ratelimit.RateLimitFilter;
import com.franco.backend.service.ITaskService;
import com.franco.backend.config.TestSecurityDisableConfig;
import static com.franco.backend.testutil.SecurityTestUtils.authenticate;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TaskController.class)
@Import({GlobalExceptionHandler.class, 
        I18nConfig.class,
        TestSecurityDisableConfig.class
    })
public class TaskControllerI18nTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ITaskService taskService;

    @MockitoBean
    CorsProperties corsProperties;

     @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtProperties jwtProperties;

    @MockitoBean
    RateLimitFilter rateLimitFilter;



    @Test
    void shouldReturnErrorMessageInEnglish() throws Exception {
        authenticate(1L);

        when(taskService.findById(99L))
            .thenThrow(ResourceNotFoundException.taskNotFound(99L));

        mockMvc.perform(get("/api/tasks/99")
                .header("Accept-Language", "en"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.message").value(
                org.hamcrest.Matchers.not("task.notFound")
            ));
    }

    @Test
    void shouldReturnErrorMessageInSpanish() throws Exception {
        authenticate(1L);

        when(taskService.findById(99L))
            .thenThrow(ResourceNotFoundException.taskNotFound(99L));

        mockMvc.perform(get("/api/tasks/99")
                .header("Accept-Language", "es"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.message").value(
                org.hamcrest.Matchers.not("task.notFound")
            ));
    }

    @Test
    void shouldFallbackToDefaultLanguage() throws Exception {
        authenticate(1L);

        when(taskService.findById(99L))
            .thenThrow(ResourceNotFoundException.taskNotFound(99L));

        mockMvc.perform(get("/api/tasks/99")
                .header("Accept-Language", "fr"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.message").value(
                org.hamcrest.Matchers.not("task.notFound")
            ));
    }


}
