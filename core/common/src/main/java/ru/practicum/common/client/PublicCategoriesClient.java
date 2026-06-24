package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.CategoryDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/categories", contextId = "publicCategoriesClient")
public interface PublicCategoriesClient {
    
    @GetMapping
    ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
    
    @GetMapping("/{catId}")
    ResponseEntity<CategoryDto> getCategory(@PathVariable("catId") Long catId);
}
