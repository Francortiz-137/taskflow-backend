package com.franco.backend.controller;

import com.franco.backend.config.SwaggerExamples;
import com.franco.backend.dto.ApiErrorResponse;
import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.service.ITaskService;
import jakarta.validation.Valid;
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
import java.util.List;

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
            description = "Tarea creada"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación",
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
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid CreateTaskRequest request) {
        
        TaskResponse response = taskService.create(request);

        return ResponseEntity
            .created(URI.create("/api/tasks/" + response.id()))
            .body(response);
    }

    // READ TASKS
    @Operation(summary = "Get a list with all tasks")
    @GetMapping("/all")
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    // READ PAGINATED TASKS
    @Operation(
    summary = "Listar tareas",
    description = """
        Devuelve una lista paginada de tareas.

        Ejemplos de sort:
        - createdAt,desc
        - title,asc
        - status,asc
        """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada")
    })
    @GetMapping
    public Page<TaskResponse> findAllPages(
        @Parameter(description = "Número de página (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Cantidad de elementos por página", example = "10")
        @RequestParam(defaultValue = "10") int size,

        @Parameter(
            description = "Sort format: field,direction (e.g. title,asc)",
            example = "createdAt,desc"
         )
        @RequestParam(defaultValue = "createdAt,desc") String sort,

        @Parameter(
            description = "Estado de la tarea",
            schema = @Schema(implementation = TaskStatus.class)
        )
        @RequestParam(required = false) TaskStatus status,

        @Parameter(
            description = "Buscar por título (contiene)",
            example = "spring"
        )
        @RequestParam(required = false) String title
    ) {
        
        return taskService.findAll(page, size, sort, status, title);
}

    // READ TASK BY ID
    @Operation(
    summary = "Obtener una tarea por id",
    description = "Devuelve una tarea existente"
)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada",
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
        @Positive(message = "id must be greater than 0")
        Long id) {
        return taskService.findById(id);
    }

    // UPDATE TASK
    @Operation(summary = "Update an existing task")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea actualizada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
         @ApiResponse(
            responseCode = "400",
            description = "Error de validación",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
         ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada",
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
            @Positive(message = "id must be greater than 0")
            Long id,

            @RequestBody 
            @Valid 
            UpdateTaskRequest request
    ) {
        return taskService.update(id, request);
    }

    // UPDATE TASK STATUS
    @Operation(summary = "Update the status of an existing task")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea encontrada y actualizada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class)
            )
        ),
        @ApiResponse(
        responseCode = "400",
        description = "Error de validación",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiErrorResponse.class)
        )
    ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PutMapping("/{id}/status")
    public TaskResponse updateStatus(
            @PathVariable 
            @Positive(message = "id must be greater than 0")
            Long id,

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
            description = "Tarea eliminada correctamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada",
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
        @Positive(message = "id must be greater than 0")
        Long id
    ) {    
            taskService.delete(id);
    }
}
