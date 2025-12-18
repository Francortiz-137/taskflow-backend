package com.franco.backend.service.impl;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.dto.CreateTaskRequest;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateTaskRequest;
import com.franco.backend.dto.UpdateTaskStatusRequest;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.repository.specification.TaskSpecifications;
import com.franco.backend.service.ITaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    private static final Set<String> ALLOWED_SORTS =
        Set.of("createdAt", "title", "status");


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
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(id));

        return mapper.toResponse(task);
    }

    @Override
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(id));

        mapper.updateEntity(request, task);

        Task saved = repository.save(task);

        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
    Task task = repository.findById(id)
            .orElseThrow(() ->
                ResourceNotFoundException.taskNotFound(id)
            );

    repository.delete(task);
}

    @Override
    public Page<TaskResponse> findAll(
            int page,
            int size,
            String sort,
            TaskStatus status,
            String title
    ) {
        String[] sortParams = sort.split(",", 2);
        String property = sortParams[0];

        if (!ALLOWED_SORTS.contains(property)) {
            throw new BadRequestException("Invalid sort property: " + property);
        }

        Sort.Direction direction =
                sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        String normalizedTitle =
                title != null && !title.isBlank() ? title.trim() : null;

        Specification<Task> spec = Specification
                .where(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.titleContains(normalizedTitle));

        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(id));

        mapper.updateStatus(request, task);

        return mapper.toResponse(repository.save(task));
    }
}
