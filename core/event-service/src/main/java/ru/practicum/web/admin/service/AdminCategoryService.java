package ru.practicum.web.admin.service;

import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

import java.util.List;

public interface AdminCategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    void delete(Long id);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long id);
}