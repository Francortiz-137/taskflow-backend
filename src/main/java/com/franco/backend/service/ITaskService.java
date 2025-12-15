package com.franco.backend.service;

import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {

    TaskResponse create(CreateTaskRequest request);

    List<TaskResponse> findAll();

    Page<TaskResponse> findAll(Pageable pageable);

    TaskResponse findById(Long id);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}
