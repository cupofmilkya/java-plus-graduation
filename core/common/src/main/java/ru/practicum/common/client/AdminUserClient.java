package ru.practicum.common.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.UserDto;
import ru.practicum.common.dto.NewUserRequest;

import java.util.List;

/**
 * Feign client for Admin Users API
 * Matches AdminUsersController from main-service
 */
@FeignClient(name = "user-service", path = "/admin/users", contextId = "adminUserClient")
public interface AdminUserClient {
    
    @PostMapping
    ResponseEntity<UserDto> create(@RequestBody @Valid NewUserRequest request);
    
    @GetMapping
    ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
    
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(@PathVariable("userId") Long userId);
}
