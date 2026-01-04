package com.franco.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static com.franco.backend.testutil.ApiExceptionAssertions.assertApiException;

import com.franco.backend.dto.task.*;
import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.exception.ErrorCode;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.service.impl.TaskServiceImpl;
import com.franco.backend.testutil.SecurityTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    TaskRepository repository;

    @Mock
    TaskMapper mapper;

    @InjectMocks
    TaskServiceImpl service;

    @AfterEach
    void cleanup() {
        SecurityTestUtils.clear();
    }

    // =========================
    // CREATE
    // =========================
    @Nested
    class CreateTask {

        @Test
        void shouldCreateTask() {
            SecurityTestUtils.authenticate(USER_ID);

            CreateTaskRequest request =
                new CreateTaskRequest("New Task", "Description");

            Task entity = new Task();
            entity.setTitle("New Task");

            Task saved = new Task();
            saved.setId(1L);

            TaskResponse response =
                new TaskResponse(
                    1L,
                    "New Task",
                    "Description",
                    TaskStatus.TODO,
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    USER_ID
                );

            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            TaskResponse result = service.create(request);

            assertThat(result.id()).isEqualTo(1L);
            verify(repository).save(entity);
        }
    }

    // =========================
    // FIND ALL (OWNERSHIP VIA SPEC)
    // =========================
    @Nested
    class FindAll {

        @Test
        void shouldReturnOnlyOwnedTasks() {
            SecurityTestUtils.authenticate(USER_ID);

            Task task = new Task();
            task.setId(1L);

            TaskResponse response =
                new TaskResponse(
                    1L,
                    "Task",
                    null,
                    TaskStatus.TODO,
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    USER_ID
                );

            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));
            when(mapper.toResponse(task)).thenReturn(response);

            Page<TaskResponse> result =
                service.findAll(0, 10, "createdAt,desc", null, null);

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findAll(
                any(Specification.class),
                any(Pageable.class)
);

        }
    }

    // =========================
    // FIND BY ID (OWNERSHIP)
    // =========================
    @Nested
    class FindById {

        @Test
        void shouldReturnTaskWhenOwned() {
            SecurityTestUtils.authenticate(USER_ID);

            Task task = new Task();
            task.setId(1L);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.of(task));

            service.findById(1L);

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
        }

        @Test
        void shouldReturn404WhenTaskBelongsToAnotherUser() {
            SecurityTestUtils.authenticate(USER_ID);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.empty());

            assertApiException(
                () -> service.findById(1L),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "task.notFound"
            );

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
            verifyNoInteractions(mapper);
        }
    }

    // =========================
    // UPDATE
    // =========================
    @Nested
    class UpdateTask {

        @Test
        void shouldUpdateOwnedTask() {
            SecurityTestUtils.authenticate(USER_ID);

            Task task = new Task();
            task.setId(1L);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.of(task));
            when(repository.save(task)).thenReturn(task);

            service.update(1L, new UpdateTaskRequest("x", null));

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
            verify(repository).save(task);
        }

        @Test
        void shouldNotUpdateTaskOfAnotherUser() {
            SecurityTestUtils.authenticate(USER_ID);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.empty());

            assertApiException(
                () -> service.update(1L, new UpdateTaskRequest("x", null)),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "task.notFound"
            );

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
        }
    }

    // =========================
    // DELETE
    // =========================
    @Nested
    class DeleteTask {

        @Test
        void shouldDeleteOwnedTask() {
            SecurityTestUtils.authenticate(USER_ID);

            Task task = new Task();
            task.setId(1L);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.of(task));

            service.delete(1L);

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
            verify(repository).delete(task);
        }

        @Test
        void shouldNotDeleteTaskOfAnotherUser() {
            SecurityTestUtils.authenticate(USER_ID);

            when(repository.findByIdAndCreatedBy(1L, USER_ID))
                .thenReturn(Optional.empty());

            assertApiException(
                () -> service.delete(1L),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "task.notFound"
            );

            verify(repository).findByIdAndCreatedBy(1L, USER_ID);
        }
    }
}
