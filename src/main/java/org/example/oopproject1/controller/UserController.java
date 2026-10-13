package org.example.oopproject1.controller;

import org.example.oopproject1.dto.CurrentUserDto;
import org.example.oopproject1.model.User;
import org.example.oopproject1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .map(user -> {
                    CurrentUserDto dto = new CurrentUserDto(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRoles()
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates the given user's email and/or password.
     * Only the user themselves may call this.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CurrentUserDto> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, String> updates,
            Authentication auth
    ) {
        // 1) Only allow the authenticated user to update their own record
        if (!userService.findByUsername(auth.getName())
                .map(User::getId)
                .filter(uid -> uid.equals(id))
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user’s profile");
        }

        // 2) Pull out fields (may be null or empty if unchanged)
        String newEmail    = updates.get("email");
        String newPassword = updates.get("password");

        // 3) Delegate to service for apply-and-save
        User updated = userService.updateUserWithPassword(id, newEmail, newPassword);

        // 4) Map back into DTO
        CurrentUserDto dto = new CurrentUserDto(
                updated.getId(),
                updated.getUsername(),
                updated.getEmail(),
                updated.getRoles()
        );
        return ResponseEntity.ok(dto);
    }
}
