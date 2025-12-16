package com.franco.backend.service;

import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;
import com.franco.backend.entity.TaskStatus;

import java.util.List;

import org.springframework.data.domain.Page;

public interface ITaskService {

    TaskResponse create(CreateTaskRequest request);

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse update(Long id, UpdateTaskRequest request);
    
    TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request);

    void delete(Long id);

    Page<TaskResponse> findAll(int page, int size, String sort, TaskStatus status, String title);
}
