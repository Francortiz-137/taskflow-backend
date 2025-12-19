package com.franco.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de la tarea")
public enum TaskStatus {

    @Schema(description = "Tarea pendiente por hacer")
    TODO {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return target == IN_PROGRESS || target == CANCELLED;
        }
    },
    @Schema(description = "Tarea en progreso")
    IN_PROGRESS {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return target == DONE || target == CANCELLED;
        }
    },
    @Schema(description = "Tarea completada")
    DONE {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return false;
        }
    },
    @Schema(description = "Tarea cancelada")
    CANCELLED {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(TaskStatus target);
}
