package ru.practicum.web.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.admin.service.AdminUserService;
import ru.practicum.common.client.AdminUserClient;
import ru.practicum.common.exception.BadRequestException;
import ru.practicum.common.dto.NewUserRequest;
import ru.practicum.common.dto.UserDto;
import ru.practicum.common.validation.ValidationConstants;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUsersController implements AdminUserClient {

    private final AdminUserService adminUserService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid NewUserRequest request) {
        UserDto created = adminUserService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = ValidationConstants.PAGE_DEFAULT_FROM + "") int from,
            @RequestParam(defaultValue = ValidationConstants.PAGE_DEFAULT_SIZE + "") int size
    ) {
        if (from < ValidationConstants.PAGE_MIN_FROM) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size < ValidationConstants.PAGE_MIN_SIZE) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }

        List<UserDto> users = adminUserService.getUsers(ids, from, size);
        return ResponseEntity.ok(users);
    }

    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
        adminUserService.delete(userId);
        return ResponseEntity.noContent().build();
    }


}