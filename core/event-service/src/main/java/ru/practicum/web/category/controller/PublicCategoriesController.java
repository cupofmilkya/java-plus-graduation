package ru.practicum.web.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.client.PublicCategoriesClient;
import ru.practicum.web.admin.service.AdminCategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
public class PublicCategoriesController implements PublicCategoriesClient {

    private final AdminCategoryService service;

    @Override
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (size <= 0 || from < 0) {
            return ResponseEntity.badRequest().build();
        }

        List<CategoryDto> categories = service.getAll(from, size);
        return ResponseEntity.ok(categories);
    }

    @Override
    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable("catId") Long catId) {
        try {
            CategoryDto category = service.getById(catId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}