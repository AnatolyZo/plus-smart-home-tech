package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl extends BaseService implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        log.trace("Инициировано получение списка всех категорий");
        List<Category> categories = categoryRepository.findAll();
        log.debug("Получен список категорий {}", categories);
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        log.trace("Инициировано создание категории");
        Category category = CategoryMapper.toCategory(request);
        Category savedCategory = categoryRepository.save(category);
        log.debug("Создана категория {}", category);
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    public CategoryDto getCategory(long categoryId) {
        log.trace("Инициировано получение категории по id");
        Category category = findEntityIn(categoryRepository, Category.class.getName(), categoryId);
        log.debug("Получена категория {} по categoryId {}", category, categoryId);
        return CategoryMapper.toCategoryDto(category);
    }
}
