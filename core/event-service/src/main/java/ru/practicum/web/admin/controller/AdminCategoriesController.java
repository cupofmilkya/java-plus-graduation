package ru.practicum.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;
import ru.practicum.common.client.AdminCategoryClient;
import ru.practicum.web.admin.service.AdminCategoryService;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoriesController implements AdminCategoryClient {

    private final AdminCategoryService service;

    @Override
    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody NewCategoryDto dto) {
        CategoryDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable Long catId,
            @RequestBody NewCategoryDto dto
    ) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(dto.getName());
        CategoryDto updated = service.update(catId, categoryDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CategoryDto> categories = service.getAll(from, size);
        return ResponseEntity.ok(categories);
    }

    @Override
    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> delete(@PathVariable("catId") Long catId) {
        service.delete(catId);
        return ResponseEntity.noContent().build();
    }
}