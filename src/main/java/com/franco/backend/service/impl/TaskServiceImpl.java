package com.franco.backend.service.impl;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.dto.task.CreateTaskRequest;
import com.franco.backend.dto.task.TaskResponse;
import com.franco.backend.dto.task.TaskSortField;
import com.franco.backend.dto.task.UpdateTaskRequest;
import com.franco.backend.dto.task.UpdateTaskStatusRequest;
import com.franco.backend.mapper.TaskMapper;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.repository.specification.TaskSpecifications;
import com.franco.backend.security.SecurityUtils;
import com.franco.backend.service.ITaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        Task task = mapper.toEntity(request);

        task.setStatus(TaskStatus.TODO);

        return mapper.toResponse(repository.save(task));
    }


    @Override
    public TaskResponse findById(Long id) {
        Long userId = SecurityUtils.currentUserId();

        Task task = repository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(id));

        return mapper.toResponse(task);
    }


    @Override
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = getOwnedTaskOrThrow(id);

        applyUpdate(task, request);

        Task saved = repository.save(task);

        return mapper.toResponse(saved);
    }


    @Override
    public void delete(Long id) {
        Task task = getOwnedTaskOrThrow(id);

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
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        String normalizedTitle = normalizeTitle(title);
        Specification<Task> spec = buildSpecification(status, normalizedTitle);

        return repository
                .findAll(spec, pageable)
                .map(mapper::toResponse);
    }


    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = getOwnedTaskOrThrow(id);

        TaskStatus current = task.getStatus();
        TaskStatus target = request.status();

        // Idempotent case, no change needed
        if(current == target) {
            return mapper.toResponse(task);
        }
        // Validate transition
        if (!current.canTransitionTo(target)) {
            throw new BadRequestException(
                "Invalid status transition: " + current + " -> " + target
            );
        }

        task.setStatus(target);

        return mapper.toResponse(repository.save(task));
    }


    // PRIVATE METHODS
    //----------------
    // FIND TASK
    //----------------

    private Sort parseSort(String sort) {
        String[] params = sort.split(",", 2);

        String property = params[0].trim();
        String directionParam =
                params.length > 1 ? params[1].trim().toLowerCase() : "desc";

        if (!TaskSortField.isValid(property)) {
            throw new BadRequestException("Invalid sort property: " + property);
        }

        if (!directionParam.equals("asc") && !directionParam.equals("desc")) {
            throw new BadRequestException("Invalid sort direction: " + directionParam);
        }

        Sort.Direction direction =
                directionParam.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, property);
    }

    private String normalizeTitle(String title) {
        return (title == null || title.isBlank()) ? null : title.trim();
    }

    private Specification<Task> buildSpecification(
        TaskStatus status,
        String title
    ) {
        Long userId = SecurityUtils.currentUserId();

        return Specification
                .where(TaskSpecifications.hasOwner(userId))
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.titleContains(title));
    }


    //----------------------
    // GET TASK OR THROW
    //----------------------
    private Task getOwnedTaskOrThrow(Long id) {
        Long userId = SecurityUtils.currentUserId();

        return repository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(id));
    }



    //----------------------
    // UPDATE TASK
    //----------------------
    private void applyUpdate(Task task, UpdateTaskRequest request) {
    if (isEmptyUpdate(request)) {
        throw new BadRequestException("At least one field must be provided for update");
    }

        mapper.updateEntity(request, task);
    }

    private boolean isEmptyUpdate(UpdateTaskRequest request) {
        return request.title() == null && request.description() == null;
    }


}
