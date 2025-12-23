package com.franco.backend.service;

import com.franco.backend.dto.task.CreateTaskRequest;
import com.franco.backend.dto.task.TaskResponse;
import com.franco.backend.dto.task.UpdateTaskRequest;
import com.franco.backend.dto.task.UpdateTaskStatusRequest;
import com.franco.backend.entity.TaskStatus;

import org.springframework.data.domain.Page;

public interface ITaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse findById(Long id);

    TaskResponse update(Long id, UpdateTaskRequest request);
    
    TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request);

    void delete(Long id);

    Page<TaskResponse> findAll(int page, int size, String sort, TaskStatus status, String title);
}
