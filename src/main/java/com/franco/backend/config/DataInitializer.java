package com.franco.backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.franco.backend.entity.Task;
import com.franco.backend.entity.TaskStatus;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.repository.TaskRepository;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.security.auth.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordService passwordService;

    @Override
    public void run(String... args) {

        log.info("Running DEV data initializer");

        if (userRepository.count() > 0) {
            log.info("Data already exists, skipping seeding");
            return;
        }

        List<User> users = seedUsers();
        seedTasks(users);

        log.info("DEV data initialization completed");
    }

    // =========================
    // USERS
    // =========================

    private List<User> seedUsers() {

        User admin = createUser(
            "Admin",
            "admin@taskflow.dev",
            "admin123",
            UserRole.ADMIN
        );

        User user1 = createUser(
            "John Doe",
            "john@taskflow.dev",
            "user1",
            UserRole.USER
        );

        User user2 = createUser(
            "Jane Doe",
            "jane@taskflow.dev",
            "user2",
            UserRole.USER
        );

        User user3 = createUser(
            "Empty",
            "empty@taskflow.dev",
            "user3",
            UserRole.USER
        );

        log.info("Seeded users: admin + 3 users");
        return List.of(admin, user1, user2, user3);
    }

    private User createUser(
        String name,
        String email,
        String rawPassword,
        UserRole role
    ) {
        return userRepository.save(
            User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordService.hash(rawPassword))
                .role(role)
                .build()
        );
    }

    // =========================
    // TASKS
    // =========================

    private void seedTasks(List<User> users) {

        if (users == null || users.isEmpty()) {
            return;
        }

        if (taskRepository.count() > 0) {
            log.info("Tasks already exist, skipping task seeding");
            return;
        }

        users.forEach(user -> {
            authenticateAs(user);
            seedTasksForUser(user);
        });

        SecurityContextHolder.clearContext();
    }

    private void seedTasksForUser(User user) {

        List<Task> tasks;

        if (user.getRole() == UserRole.ADMIN) {
            tasks = List.of(
                buildTask("Review system logs", "Check security & auth logs", TaskStatus.IN_PROGRESS),
                buildTask("Prepare production release", "Finalize release checklist", TaskStatus.TODO)
            );
        } else if (!user.getEmail().startsWith("empty")) {
            tasks = List.of(
                buildTask("Finish frontend UI", "Implement dashboard", TaskStatus.TODO),
                buildTask("Fix login bug", "Token refresh edge case", TaskStatus.DONE)
            );
        } else {
            log.info("User {} has no initial tasks", user.getEmail());
            return;
        }

        taskRepository.saveAll(tasks);
        log.info("Seeded {} tasks for {}", tasks.size(), user.getEmail());
    }

    private Task buildTask(String title, String description, TaskStatus status) {
        return Task.builder()
            .title(title)
            .description(description)
            .status(status)
            .build();
    }

    // =========================
    // AUTH CONTEXT
    // =========================

    private void authenticateAs(User user) {

        UserPrincipal principal = new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getRole()
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
