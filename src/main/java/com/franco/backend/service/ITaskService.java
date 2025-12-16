package com.franco.backend.service;

import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {

    TaskResponse create(CreateTaskRequest request);

    List<TaskResponse> findAll();

    Page<TaskResponse> findAll(Pageable pageable);

    TaskResponse findById(Long id);

    TaskResponse update(Long id, UpdateTaskRequest request);
    
    TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request);

    void delete(Long id);
}
