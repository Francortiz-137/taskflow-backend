package com.franco.backend.entity;

public enum TaskStatus {

    TODO {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return target == IN_PROGRESS || target == CANCELLED;
        }
    },
    IN_PROGRESS {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return target == DONE || target == CANCELLED;
        }
    },
    DONE {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitionTo(TaskStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(TaskStatus target);
}
