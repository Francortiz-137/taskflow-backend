package com.franco.backend.mapper;

import com.franco.backend.entity.Task;
import com.franco.backend.dto.TaskRequest;
import com.franco.backend.dto.TaskResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    Task toEntity(TaskRequest request);

    TaskResponse toResponse(Task task);
}
