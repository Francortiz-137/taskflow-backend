package com.franco.backend.controller;

import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.service.ITaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@Tag(name = "Tasks", description = "Task management endpoints")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService service;

    @Operation(summary = "Create a new task")
    @PostMapping
    public TaskResponse create(@RequestBody @Valid TaskRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Get a list with all tasks")
    @GetMapping
    public List<TaskResponse> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Get a task by its ID")
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Update an existing task")
    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable Long id,
            @RequestBody @Valid TaskRequest request
    ) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a task by its ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
