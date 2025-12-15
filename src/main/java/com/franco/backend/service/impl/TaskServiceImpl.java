package com.franco.backend.service.impl;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.service.ITaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        Task task = mapper.toEntity(request);

        task.setStatus(TaskStatus.TODO);
        task.setCreatedAt(OffsetDateTime.now());

        return mapper.toResponse(repository.save(task));
    }

    @Override
    public List<TaskResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TaskResponse findById(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return mapper.toResponse(task);
    }

    @Override
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());

        return mapper.toResponse(repository.save(task));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<TaskResponse> findAll(Pageable pageable) {
        
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }
}
