package com.franco.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.count() > 0) {
            log.info("Skipping data initialization (users already exist)");
            return;
        }

        User admin = User.builder()
            .name("Admin")
            .email("admin@taskflow.dev")
            .passwordHash(passwordEncoder.encode("admin123"))
            .role(UserRole.ADMIN)
            .build();

        userRepository.save(admin);

        log.info("Dev admin user created: {}", admin.getEmail());

        // tasks demo
        Task task = Task.builder()
            .title("First Task")
            .description("Test")
            .status(TaskStatus.TODO)
            .build();

        taskRepository.save(task);

    }
}

