package com.franco.backend.service;

import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;

import java.util.List;

public interface ITaskService {

    TaskResponse create(TaskRequest request);

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}
