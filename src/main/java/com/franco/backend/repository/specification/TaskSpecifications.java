package com.franco.backend.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;

public class TaskSpecifications {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> titleContains(String title) {
        return (root, query, cb) ->
                (title == null || title.isBlank())
                        ? null
                        : cb.like(
                            cb.lower(root.get("title")),
                            "%" + title.toLowerCase() + "%"
                        );
    }
}
