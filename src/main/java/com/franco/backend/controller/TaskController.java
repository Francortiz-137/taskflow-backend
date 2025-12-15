package com.franco.backend.controller;

import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.service.ITaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@Tag(name = "Tasks", description = "Task management endpoints")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    @Operation(summary = "Create a new task")
    @PostMapping
    public TaskResponse create(@RequestBody @Valid CreateTaskRequest request) {
        return taskService.create(request);
    }

    @Operation(summary = "Get a list with all tasks")
    @GetMapping("/all")
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    @Operation(summary = "Get paginated tasks")
    @GetMapping
    public Page<TaskResponse> findAllPages(
            @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @Parameter(
            description = "Sort format: field,direction (e.g. title,asc)",
            example = "createdAt,desc"
         )
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
    String[] sortParams = sort.split(",");
    Sort.Direction direction = Sort.Direction.fromOptionalString(
            sortParams.length > 1 ? sortParams[1] : "asc"
    ).orElse(Sort.Direction.ASC);

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(direction, sortParams[0])
    );

    return taskService.findAll(pageable);
}

    @Operation(summary = "Get a task by its ID")
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @Operation(summary = "Update an existing task")
    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable Long id,
            @RequestBody @Valid TaskRequest request
    ) {
        return taskService.update(id, request);
    }

    @Operation(summary = "Delete a task by its ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
