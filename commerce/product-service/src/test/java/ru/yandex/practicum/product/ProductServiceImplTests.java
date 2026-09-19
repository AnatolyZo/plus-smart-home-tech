package ru.yandex.practicum.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;
import ru.yandex.practicum.product.service.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product mockProduct;
    private Category mockCategory;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .build();

        mockCategory = Category.builder()
                .id(10L)
                .name("Electronics")
                .build();

        mockProduct.setCategory(mockCategory);

        createRequest = new CreateProductRequest("New Product",
                "Description",
                BigDecimal.valueOf(200.0),
                10L,
                "image.jpg");
        updateRequest = new UpdateProductRequest(null,
                null,
                BigDecimal.valueOf(300.0),
                null,
                null,
                true);
    }

    @Test
    void getAllActiveProducts_returnsList() {
        List<Product> activeProducts = List.of(mockProduct);
        when(productRepository.findByActiveTrue()).thenReturn(activeProducts);

        List<ProductDto> result = productService.getAllActiveProducts();

        verify(productRepository, times(1)).findByActiveTrue();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
        assertThat(result.get(0).price()).isEqualTo(BigDecimal.valueOf(100.0));
    }

    @Test
    void createProduct_createsAndReturnsDto() {
        Product savedProduct = Product.builder()
                .id(2L)
                .name(createRequest.name())
                .price(createRequest.price())
                .category(mockCategory)
                .build();

        when(categoryRepository.findById(eq(10L))).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.createProduct(createRequest);

        verify(categoryRepository, times(1)).findById(10L);
        verify(productRepository, times(1)).save(any(Product.class));

        assertThat(result.name()).isEqualTo(createRequest.name());
        assertThat(result.price()).isEqualTo(createRequest.price());
        assertThat(result.category().id()).isEqualTo(mockCategory.getId());
    }

    @Test
    void getProductById_returnsDto() {
        when(productRepository.findById(eq(1L))).thenReturn(Optional.of(mockProduct));

        ProductDto result = productService.getProductById(1L);

        verify(productRepository, times(1)).findById(1L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Product");
    }

    @Test
    void updateProduct_updatesOnlyNonNullFields() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Old Name");
        existingProduct.setDescription("Old Desc");
        existingProduct.setPrice(BigDecimal.valueOf(50.0));
        existingProduct.setCategory(mockCategory);
        existingProduct.setActive(false);

        when(productRepository.findById(eq(1L))).thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.updateProduct(1L, updateRequest);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));

        assertThat(existingProduct.getName()).isEqualTo("Old Name");
        assertThat(existingProduct.getDescription()).isEqualTo("Old Desc");
        assertThat(existingProduct.getPrice()).isEqualTo(BigDecimal.valueOf(300.0));
        assertThat(existingProduct.isActive()).isTrue();
    }

    @Test
    void searchProducts_findsContaining() {
        List<Product> found = List.of(mockProduct);
        when(productRepository.findByNameContainingIgnoreCase(eq("test"))).thenReturn(found);

        List<ProductDto> result = productService.searchProducts("test");

        verify(productRepository, times(1)).findByNameContainingIgnoreCase("test");
        assertThat(result).hasSize(1);
    }

    @Test
    void getProductsByCategory_returnsFilteredList() {
        List<Product> productsInCategory = List.of(mockProduct);
        when(productRepository.findByCategoryId(eq(10L))).thenReturn(productsInCategory);

        List<ProductDto> result = productService.getProductsByCategory(10L);

        verify(productRepository, times(1)).findByCategoryId(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).category().id()).isEqualTo(10L);
    }

    @Test
    void createProduct_throwsWhenCategoryNotFound() {
        when(categoryRepository.findById(eq(999L))).thenReturn(Optional.empty());

        CreateProductRequest badRequest = new CreateProductRequest("Bad", "Desc", BigDecimal.valueOf(10.0), 999L, null);

        assertThrows(NotFoundException.class,
                () -> productService.createProduct(badRequest));

        verify(categoryRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any());
    }
}