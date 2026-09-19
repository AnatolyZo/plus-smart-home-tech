package ru.yandex.practicum.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.service.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getAllCategories_returnsList() {
        Category cat1 = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        Category cat2 = Category.builder()
                .id(2L)
                .name("Books")
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Electronics");
        assertThat(result.get(1).name()).isEqualTo("Books");
        verify(categoryRepository).findAll();
    }

    @Test
    void getAllCategories_emptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
        verify(categoryRepository).findAll();
    }

    @Test
    void createCategory_savesAndReturnsDto() {
        CreateCategoryRequest request = new CreateCategoryRequest("Toys", "Description");

        Category savedCategory = Category.builder()
                .id(10L)
                .name("Toys")
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryDto result = categoryService.createCategory(request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Toys");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getCategory_returnsDto() {
        Category category = Category.builder()
                .id(5L)
                .name("Clothing")
                .build();

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategory(5L);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Clothing");
        verify(categoryRepository).findById(5L);
    }

    @Test
    void getCategory_throwsWhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> categoryService.getCategory(999L));

        verify(categoryRepository).findById(999L);
    }
}