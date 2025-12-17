package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import com.franco.backend.config.CorsProperties;
import com.franco.backend.config.GlobalExceptionHandler;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.service.impl.TaskServiceImpl;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TaskServiceImpl taskService;

    @MockitoBean
    TaskMapper taskMapper;

    @MockitoBean
    CorsProperties corsProperties;

    @Test
    void shouldListTasks() throws Exception {
        TaskResponse task = new TaskResponse(1L, "Aprender Spring", null, TaskStatus.TODO, OffsetDateTime.now(), OffsetDateTime.now());

        Page<TaskResponse> page = new PageImpl<>(List.of(task));

       when(taskService.findAll(
        anyInt(),
        anyInt(),
        eq("createdAt,desc"),
        isNull(),
        isNull()
        )).thenReturn(page);



        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Aprender Spring"))
                .andExpect(jsonPath("$.content[0].status").value("TODO"));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
        TaskResponse task = new TaskResponse(2L, "Task Done", null, TaskStatus.DONE, OffsetDateTime.now(), OffsetDateTime.now());

        Page<TaskResponse> page = new PageImpl<>(List.of(task));

        when(taskService.findAll(
                anyInt(),
                anyInt(),
                eq("createdAt,desc"),
                eq(TaskStatus.DONE),
                isNull()
        )).thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("DONE"));
    }

    @Test
    void shouldFilterByTitle() throws Exception {
        TaskResponse task = new TaskResponse(3L, "Learn Testing", null, TaskStatus.TODO, OffsetDateTime.now(), OffsetDateTime.now());

        Page<TaskResponse> page = new PageImpl<>(List.of(task));

        when(taskService.findAll(
        anyInt(),
        anyInt(),
        eq("createdAt,desc"),
        isNull(),
        eq("Testing")
        )).thenReturn(page);


        mockMvc.perform(get("/api/tasks")
                .param("title", "Testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Learn Testing"));
    }

    @Test
    void shouldApplyPaginationAndSorting() throws Exception {
        TaskResponse task1 = new TaskResponse(4L, "Task A", null, TaskStatus.TODO, OffsetDateTime.now(), OffsetDateTime.now());
        TaskResponse task2 = new TaskResponse(5L, "Task B", null, TaskStatus.TODO, OffsetDateTime.now(), OffsetDateTime.now());

        Page<TaskResponse> page = new PageImpl<>(List.of(task1, task2));

        when(taskService.findAll(
                eq(1),
                eq(2),
                eq("title,asc"),
                isNull(),
                isNull()
        )).thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                .param("page", "1")
                .param("size", "2")
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(4))
                .andExpect(jsonPath("$.content[1].id").value(5));
    }

    @Test
    void shouldFailWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "title": ""
                    }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("title: must not be blank"));
    }


}