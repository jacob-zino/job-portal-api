// src/main/java/org/example/oopproject1/controller/AdminController.java
package org.example.oopproject1.controller;

import org.example.oopproject1.dto.CurrentUserDto;
import org.example.oopproject1.model.User;
import org.example.oopproject1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/admin/users
     * Returns all users as DTOs (id, username, email, roles).
     */
    @GetMapping("/users")
    public ResponseEntity<List<CurrentUserDto>> getAllUsers() {
        List<CurrentUserDto> dtos = userService.getAllUsers().stream()
                .map(u -> new CurrentUserDto(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRoles()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/admin/users/{id}
     * Returns a single user as DTO.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<CurrentUserDto> getUserById(@PathVariable String id) {
        User u = userService.getUserById(id); // throws if not found
        CurrentUserDto dto = new CurrentUserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRoles()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/admin/users/{id}/status?enabled={true|false}
     * Enables or disables the given user.
     * Returns 204 No Content on success.
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable String id,
            @RequestParam boolean enabled
    ) {
        userService.updateUserStatus(id, enabled);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/stats
     * Returns admin dashboard stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(userService.getDashboardStats());
    }
}
