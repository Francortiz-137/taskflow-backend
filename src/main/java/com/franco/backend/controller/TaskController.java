package com.franco.backend.controller;

import com.franco.backend.config.SwaggerExamples;
import com.franco.backend.dto.common.ApiErrorResponse;
import com.franco.backend.dto.common.PageResponse;
import com.franco.backend.dto.task.CreateTaskRequest;
import com.franco.backend.dto.task.TaskResponse;
import com.franco.backend.dto.task.UpdateTaskRequest;
import com.franco.backend.dto.task.UpdateTaskStatusRequest;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.service.ITaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

@Tag(name = "Tasks", description = "Task management endpoints")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final ITaskService taskService;

    // CREATE TASK
    @Operation(summary = "Create a new task")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        )
    })
    @PostMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid CreateTaskRequest request) {
        
        TaskResponse response = taskService.create(request);

        return ResponseEntity
            .created(URI.create("/api/tasks/" + response.id()))
            .body(response);
    }

    // READ PAGINATED TASKS
    @Operation(
    summary = "List paginated tasks",
    description = """
        Returns a paginated list of tasks with optional filtering by status and title.

        Sorting format: field,direction (e.g. title,asc).
        Allowed sort fields:
        - createdAt,desc
        - title,asc
        - status,asc
        """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of tasks")
    })
    @GetMapping
    public PageResponse<TaskResponse> findAllPages(
        @Parameter(description = "Page (0-based)", example = "0")
        @RequestParam(defaultValue = "0") 
        @Min(value = 0, message = "{validation.page.min}")
        int page,


        @Parameter(description = "Elements per page", example = "10")
        @RequestParam(defaultValue = "10") 
        @Min(value = 1, message = "{validation.size.min}")
        @Max(value = 100, message = "{validation.size.max}")
        int size,


        @Parameter(
            description = "Sort format: field,direction (e.g. title,asc)",
            example = "createdAt,desc"
         )
        @RequestParam(defaultValue = "createdAt,desc") 
        String sort,


        @Parameter(
            description = "Task status to filter by", example = "TODO",
            schema = @Schema(implementation = TaskStatus.class)
        )
        @RequestParam(required = false) 
        TaskStatus status,


        @Parameter(
            description = "Title substring to filter by",
            example = "spring"
        )
        @RequestParam(required = false) 
        String title
    ) {
        Page<TaskResponse> pageResponse = taskService.findAll(page, size, sort, status, title);

        return new PageResponse<>(
            pageResponse.getContent(),
            pageResponse.getNumber(),
            pageResponse.getSize(),
            pageResponse.getTotalElements(),
            pageResponse.getTotalPages()
        );
}

    // READ TASK BY ID
    @Operation(
    summary = "Get a task by its ID",
    description = "Returns an existing task"
)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @GetMapping("/{id}")
    public TaskResponse findById(
        @PathVariable 
        @Positive(message = "{validation.id.positive}")
        Long id) {
        return taskService.findById(id);
    }

    // UPDATE TASK
    @Operation(summary = "Update an existing task")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
         @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
         ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable 
            @Positive(message = "{validation.id.positive}")
            Long id,

            @RequestBody 
            @Valid 
            UpdateTaskRequest request
    ) {
        return taskService.update(id, request);
    }

    // UPDATE TASK STATUS
    @Operation(
        summary = "Update task status",
        description = """
        Allowed transitions:
        - TODO → IN_PROGRESS, CANCELLED
        - IN_PROGRESS → DONE, CANCELLED
        - DONE / CANCELLED → no transitions allowed
        This operation is idempotent. 
        If the task already has the requested status, no changes are made.
        """
    )

    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
        @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiErrorResponse.class)
        )
    ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PutMapping("/{id}/status")
    public TaskResponse updateStatus(
            @Parameter(description = "ID of the task", example = "1")
            @PathVariable 
            @Positive(message = "{validation.id.positive}")
            Long id,

            @Parameter(description = "New status of the task")
            @RequestBody
            @Valid 
            UpdateTaskStatusRequest request
    ) {
        return taskService.updateStatus(id, request);
    }

    // DELETE TASK
    @Operation(summary = "Delete a task by its ID")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Task deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(
        @PathVariable        
        @Positive(message = "{validation.id.positive}")
        Long id
    ) {    
            taskService.delete(id);
    }
}
