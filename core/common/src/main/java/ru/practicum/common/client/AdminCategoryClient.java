package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

/**
 * Feign client for Admin Categories API
 * Matches AdminCategoriesController from main-service
 */
@FeignClient(name = "event-service", path = "/admin/categories", contextId = "adminCategoryClient")
public interface AdminCategoryClient {
    
    @PostMapping
    ResponseEntity<CategoryDto> create(@RequestBody NewCategoryDto dto);
    
    @PatchMapping("/{catId}")
    ResponseEntity<CategoryDto> update(
            @PathVariable("catId") Long catId,
            @RequestBody NewCategoryDto dto
    );
    
    @DeleteMapping("/{catId}")
    ResponseEntity<Void> delete(@PathVariable("catId") Long catId);
}
