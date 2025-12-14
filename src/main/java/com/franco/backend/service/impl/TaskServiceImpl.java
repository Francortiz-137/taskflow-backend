package com.franco.backend.service.impl;

import com.franco.backend.entity.Task;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    @Override
    public TaskResponse create(TaskRequest request) {
        Task task = mapper.toEntity(request);
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

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());

        return mapper.toResponse(repository.save(task));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
