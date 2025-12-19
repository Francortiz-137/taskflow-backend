package com.franco.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;
import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.service.impl.TaskServiceImpl;

import com.franco.backend.exception.ApiException;
import com.franco.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;



@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private TaskMapper mapper;

    @InjectMocks
    private TaskServiceImpl service;

    @Nested
    class CreateTask {

        @Test
        void shouldCreateTask() {
            CreateTaskRequest request = new CreateTaskRequest(
                    "New Task",
                    "Description"
            );

            Task task = new Task();
            task.setTitle("New Task");
            task.setDescription("Description");
            task.setStatus(TaskStatus.TODO);

            Task savedTask = new Task();
            savedTask.setId(1L);
            savedTask.setTitle("New Task");
            savedTask.setDescription("Description");
            savedTask.setStatus(TaskStatus.TODO);

            TaskResponse response = new TaskResponse(
                    1L,
                    "New Task",
                    "Description",
                    TaskStatus.TODO,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );

            when(mapper.toEntity(request)).thenReturn(task);
            when(repository.save(task)).thenReturn(savedTask);
            when(mapper.toResponse(savedTask)).thenReturn(response);

            TaskResponse result = service.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("New Task");
            assertThat(result.status()).isEqualTo(TaskStatus.TODO);

            verify(mapper).toEntity(request);
            verify(repository).save(task);
            verify(mapper).toResponse(savedTask);
        }

        @Test
        void shouldCreateTaskWithNullDescription() {
            CreateTaskRequest request = new CreateTaskRequest(
                    "New Task",
                    null
            );          
            Task task = new Task();
            task.setTitle("New Task");
            task.setDescription(null);
            task.setStatus(TaskStatus.TODO);
            Task savedTask = new Task();
            savedTask.setId(1L);
            savedTask.setTitle("New Task");
            savedTask.setDescription(null);
            savedTask.setStatus(TaskStatus.TODO);
            TaskResponse response = new TaskResponse(
                    1L,
                    "New Task",
                    null,
                    TaskStatus.TODO,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );
            when(mapper.toEntity(request)).thenReturn(task);
            when(repository.save(task)).thenReturn(savedTask);
            when(mapper.toResponse(savedTask)).thenReturn(response);
            TaskResponse result = service.create(request);
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("New Task");
            assertThat(result.description()).isNull();
            assertThat(result.status()).isEqualTo(TaskStatus.TODO);
            verify(mapper).toEntity(request);
            verify(repository).save(task);
            verify(mapper).toResponse(savedTask);
        }

    }

    @Nested
    class FindAll { 
        @Test
        void shouldReturnAllTasks() {
            Task task1 = new Task();
            task1.setId(1L);
            task1.setTitle("Task 1");

            Task task2 = new Task();
            task2.setId(2L);
            task2.setTitle("Task 2");

            TaskResponse response1 = new TaskResponse(
                    1L, "Task 1", null, TaskStatus.TODO,
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            TaskResponse response2 = new TaskResponse(
                    2L, "Task 2", null, TaskStatus.TODO,
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(task1, task2)));
            when(mapper.toResponse(task1)).thenReturn(response1);
            when(mapper.toResponse(task2)).thenReturn(response2);

            Page<TaskResponse> results =
                    service.findAll(0, 10, "createdAt,desc", null, null);

            assertThat(results.getContent()).hasSize(2);
            assertThat(results.getContent().get(0).id()).isEqualTo(1L);
            assertThat(results.getContent().get(1).id()).isEqualTo(2L);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verify(mapper).toResponse(task1);
            verify(mapper).toResponse(task2);
        }


        @Test
        void shouldReturnEmptyPageWhenNoTasksExist() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<TaskResponse> results =
                    service.findAll(0, 10, "createdAt,desc", null, null);

            assertThat(results.getContent()).isEmpty();

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoInteractions(mapper);
        }


        @Test
        void shouldApplyStatusAndTitleFilters() {
            Task task = new Task();
            task.setId(2L);
            task.setTitle("Learn Spring");
            task.setStatus(TaskStatus.DONE);

            TaskResponse response = new TaskResponse(
                    2L,
                    "Learn Spring",
                    null,
                    TaskStatus.DONE,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );

            Page<Task> page = new PageImpl<>(List.of(task));

            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(mapper.toResponse(task)).thenReturn(response);

            Page<TaskResponse> result =
                    service.findAll(0, 10, "title,asc", TaskStatus.DONE, "Spring");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(TaskStatus.DONE);
            assertThat(result.getContent().get(0).title()).contains("Spring");

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verify(mapper).toResponse(task);
        }

        @Test
        void shouldTrimTitleBeforeFiltering() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            service.findAll(0, 10, "createdAt,desc", null, "   spring   ");

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
        }


        @Test
        void shouldThrowWhenSortDirectionIsInvalid() {
            assertThatThrownBy(() ->
                service.findAll(0, 10, "title,invalid", null, null)
            )
            .isInstanceOf(BadRequestException.class)
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
            });

        }

        @Test
        void shouldThrowWhenSortPropertyIsInvalid() {
            assertThatThrownBy(() ->
                service.findAll(0, 10, "invalid,asc", null, null)
            )
            .isInstanceOf(BadRequestException.class)
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
            });


            verifyNoInteractions(repository, mapper);
        }


    }

    @Nested
    class FindById {

        @Test
        void shouldReturnTaskWhenExists() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Task");

            TaskResponse response = new TaskResponse(
                    1L,
                    "Task",
                    null,
                    TaskStatus.TODO,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );

            when(repository.findById(1L)).thenReturn(Optional.of(task));
            when(mapper.toResponse(task)).thenReturn(response);

            TaskResponse result = service.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("Task");

            verify(repository).findById(1L);
            verify(mapper).toResponse(task);
        }

        @Test
        void shouldThrowWhenTaskDoesNotExist() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                assertThat(apiEx.getMessage()).isEqualTo("task.notFound");
            });

            verify(repository).findById(99L);
            verifyNoInteractions(mapper);
        }

    }


    @Nested
    class UpdateTask {

    @Test
    void shouldUpdateTask() {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated title",
                "Updated description"
        );

        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old title");

        TaskResponse response = new TaskResponse(
                1L,
                "Updated title",
                "Updated description",
                TaskStatus.TODO,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        TaskResponse result = service.update(1L, request);

        assertThat(result.title()).isEqualTo("Updated title");

        verify(repository).findById(1L);
        verify(mapper).updateEntity(request, existing);
        verify(repository).save(existing);
        verify(mapper).toResponse(existing);
    }
    @Test
    void shouldThrowWhenUpdatingNonExistingTask() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateTaskRequest("t", null)))
        .isInstanceOf(ResourceNotFoundException.class)
        .satisfies(ex -> {
            ApiException apiEx = (ApiException) ex;
            assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
            assertThat(apiEx.getMessage()).isEqualTo("task.notFound");
        });

        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }
}


    @Nested
    class UpdateStatus {
        //--------------------
        // HAPPY PATH
        //--------------------
    @Test
    void shouldUpdateTaskStatus() {
        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.DONE);

        Task existing = new Task();
        existing.setId(1L);
        existing.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse response = new TaskResponse(
                1L,
                "Task",
                null,
                TaskStatus.DONE,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        TaskResponse result = service.updateStatus(1L, request);

        assertThat(result.status()).isEqualTo(TaskStatus.DONE);

        verify(repository).findById(1L);
        verify(repository).save(existing);
        verify(mapper).toResponse(existing);
    }

    @Test
    void shouldAllowValidStatusTransition() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.TODO);

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(
            new TaskResponse(
                1L, "Task", null, TaskStatus.IN_PROGRESS,
                OffsetDateTime.now(), OffsetDateTime.now()
            )
        );

        TaskResponse result = service.updateStatus(
            1L,
            new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS)
        );

        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldBeIdempotentWhenStatusIsSame() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.DONE);

        TaskResponse response = new TaskResponse(
            1L,
            "Task",
            null,
            TaskStatus.DONE,
            OffsetDateTime.now().minusDays(1),
            OffsetDateTime.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(mapper.toResponse(task)).thenReturn(response);

        TaskResponse result = service.updateStatus(
            1L,
            new UpdateTaskStatusRequest(TaskStatus.DONE)
        );

        assertThat(result.status()).isEqualTo(TaskStatus.DONE);

        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    //--------------------
    // ERROR CASES
    //--------------------

    @Test
    void shouldFailWhenTransitionIsInvalid() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.DONE);

        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
            service.updateStatus(1L, new UpdateTaskStatusRequest(TaskStatus.TODO))
        )
        .isInstanceOf(BadRequestException.class)
        .satisfies(ex -> {
            ApiException apiEx = (ApiException) ex;
            assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        });


        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }



    @Test
    void shouldThrowWhenUpdatingStatusOfNonExistingTask() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L,
        new UpdateTaskStatusRequest(TaskStatus.DONE)))
        .isInstanceOf(ResourceNotFoundException.class)
        .satisfies(ex -> {
            ApiException apiEx = (ApiException) ex;
            assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
            assertThat(apiEx.getMessage()).isEqualTo("task.notFound");
        });


        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldThrowWhenUpdateRequestIsEmpty() {
        Task task = new Task();
        task.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
            service.update(1L, new UpdateTaskRequest(null, null))
        )
        .isInstanceOf(BadRequestException.class)
        .satisfies(ex -> {
            ApiException apiEx = (ApiException) ex;
            assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        });


        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFailWhenTransitionIsInvalidEvenIfTargetWasUsedBefore() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.CANCELLED);

        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
            service.updateStatus(1L, new UpdateTaskStatusRequest(TaskStatus.DONE))
        )
        .isInstanceOf(BadRequestException.class)
        .satisfies(ex -> {
            ApiException apiEx = (ApiException) ex;
            assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        });


        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }


}


    @Nested
    class DeleteTask {

        @Test
        void shouldDeleteTask() {
            Task task = new Task();
            task.setId(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(task));
            doNothing().when(repository).delete(task);

            service.delete(1L);

            verify(repository).findById(1L);
            verify(repository).delete(task);
        }
        
            @Test
        void shouldThrowWhenDeletingNonExistingTask() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(apiEx.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                assertThat(apiEx.getMessage()).isEqualTo("task.notFound");
            });

            verify(repository).findById(99L);
            verifyNoMoreInteractions(repository);
        }
    }


}
