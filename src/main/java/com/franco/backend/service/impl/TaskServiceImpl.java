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
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return mapper.toResponse(task);
    }

    @Override
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        task.setTitle(request.title());
        task.setDescription(request.description());

        return mapper.toResponse(repository.save(task));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<TaskResponse> findAll(
        int page,
        int size,
        String sort,
        TaskStatus status,
        String title
) {
    final Set<String> ALLOWED_SORTS = Set.of("createdAt", "title", "status");

    if (!ALLOWED_SORTS.contains(sort.split(",")[0])) {
        throw new BadRequestException("Invalid sort property: " + sort.split(",")[0]);
    }

    String[] sortParams = sort.split(",");
    String property = sortParams[0];
    Sort.Direction direction =
            sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

    Specification<Task> spec = Specification
            .where(TaskSpecifications.hasStatus(status))
            .and(TaskSpecifications.titleContains(title));

    return repository
            .findAll(spec, pageable)
            .map(mapper::toResponse);
}

    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        mapper.updateStatus(request, task);

        return mapper.toResponse(repository.save(task));
    }
}
