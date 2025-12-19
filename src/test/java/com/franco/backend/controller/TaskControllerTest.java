package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.CorsProperties;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.exception.ResourceNotFoundException;
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

    private final OffsetDateTime now = OffsetDateTime.now();



    // =========================
    // POST /api/tasks
    // =========================

     @Nested
     @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        void shouldCreateTask() throws Exception {
        TaskResponse created = new TaskResponse(
                1L,
                "New Task",     
                "Task description",
                TaskStatus.TODO,
                now.minusDays(1),
                now
        );      

        when(taskService.create(any()))
                .thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "title": "New Task",
                    "description": "Task description"
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.status").value("TODO"));
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

    // =========================
    // GET /api/tasks
    // =========================
    @Nested
    @DisplayName("GET /api/tasks")
    class GetTasks {

         @Test
        void shouldListTasks() throws Exception {
                TaskResponse task = new TaskResponse(
                        1L, 
                        "Aprender Spring", 
                        null, 
                        TaskStatus.TODO, 
                        now.minusDays(1), 
                        now);

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
                TaskResponse task = new TaskResponse(
                        2L,
                        "Task Done", 
                        null, 
                        TaskStatus.DONE, 
                        now.minusDays(1), 
                        now);

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
                TaskResponse task = new TaskResponse(
                        3L, 
                        "Learn Testing", 
                        null, 
                        TaskStatus.TODO, 
                        now.minusDays(1), 
                        now);

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
                TaskResponse task1 = new TaskResponse(4L, 
                        "Task A", 
                        null, 
                        TaskStatus.TODO, 
                        now.minusDays(1), 
                        now);
                TaskResponse task2 = new TaskResponse(5L, 
                        "Task B", 
                        null, 
                        TaskStatus.TODO, 
                        now.minusDays(1), 
                        now);

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
        void shouldFailWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
        
        @Test
        void shouldFailWhenSizeIsTooLarge() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("size", "500"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWhenSortDirectionIsInvalid() throws Exception {

        when(taskService.findAll(
                anyInt(),
                anyInt(),
                eq("title,invalid"),
                isNull(),
                isNull()
        )).thenThrow(new BadRequestException("Invalid sort direction: invalid"));

        mockMvc.perform(get("/api/tasks")
                .param("sort", "title,invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid sort direction: invalid"));
        }

    }
    
    // =========================
    // GET /api/tasks/{id}
    // =========================

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetTasksById {

    @Test
    void shouldReturnTaskById() throws Exception {
        TaskResponse task = new TaskResponse(
                1L, 
                "Task", 
                null, 
                TaskStatus.TODO, 
                now.minusDays(1), 
                now
        );

        when(taskService.findById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Task with id 99 not found"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Task with id 99 not found"));
    }

    @Test
    void shouldFailWhenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/tasks/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    }

    // =========================
    // PUT /api/tasks/{id}
    // =========================

    @DisplayName("PUT /api/tasks/{id}")
    @Nested
    class UpdateTask {
        @Test
        void shouldUpdateTask() throws Exception {
        TaskResponse updated = new TaskResponse(
                1L,
                "Updated title",
                "Updated desc",
                TaskStatus.TODO,
                now.minusDays(1),
                now
        );

        when(taskService.update(eq(1L), any(UpdateTaskRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "Updated title",
                        "description": "Updated desc"
                        }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistingTask() throws Exception {
        when(taskService.update(eq(99L), any(UpdateTaskRequest.class)))
                .thenThrow(new ResourceNotFoundException("Task with id 99 not found"));

        mockMvc.perform(put("/api/tasks/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "Title"
                        }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        void shouldFailWhenTitleIsBlankOnUpdate() throws Exception {
        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": ""
                        }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenUpdatingWithInvalidId() throws Exception {
        mockMvc.perform(put("/api/tasks/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "Valid title"
                        }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    // =========================
    // PUT /api/tasks/{id}/status
    // =========================
    @Nested
    @DisplayName("PUT /api/tasks/{id}/status")
    class UpdateTaskStatus {
    
        @Test
        void shouldUpdateTaskStatus() throws Exception {
        TaskResponse updated = new TaskResponse(
                1L,
                "Task",
                null,
                TaskStatus.DONE,
                now.minusDays(1),
                now
        );

        when(taskService.updateStatus(eq(1L), any(UpdateTaskStatusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "status": "DONE"
                        }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
        }

        @Test
        void shouldFailWhenStatusIsMissing() throws Exception {
        mockMvc.perform(put("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenStatusIsInvalid() throws Exception {
        mockMvc.perform(put("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "status": "INVALID"
                        }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("status: invalid value"));
        }

        @Test
        void shouldReturn404WhenUpdatingStatusOfNonExistingTask() throws Exception {
        when(taskService.updateStatus(eq(99L), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new ResourceNotFoundException("Task with id 99 not found"));

        mockMvc.perform(put("/api/tasks/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "status": "DONE"
                        }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        void shouldFailWhenUpdatingStatusWithInvalidId() throws Exception {
        mockMvc.perform(put("/api/tasks/0/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "status": "DONE"
                        }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        }


    // =========================
    // DELETE /api/tasks/{id}
    // =========================
    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {    

        @Test
        void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenDeletingNonExistingTask() throws Exception {
        doThrow(new ResourceNotFoundException("Task with id 99 not found"))
                .when(taskService).delete(99L);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        void shouldFailWhenDeletingWithInvalidId() throws Exception {
        mockMvc.perform(delete("/api/tasks/0"))
                .andExpect(status().isBadRequest());
        }
    }
}