package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl extends BaseService implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductDto> getAllActiveProducts() {
        log.trace("Инициировано получение списка всех активных товаров");
        List<Product> products = productRepository.findByActiveTrue();
        log.debug("Получен список активных товаров {}", products);
        return convertToListProductDto(products);
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        log.trace("Инициировано создание товара");
        Category category = findEntityIn(categoryRepository, Category.class.getName(), request.categoryId());
        Product product = ProductMapper.toProduct(request, category);
        Product savedProduct = productRepository.save(product);
        log.debug("Создан товар {}", savedProduct);
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(savedProduct.getCategory());
        return ProductMapper.toProductDto(savedProduct, categoryDto);
    }

    @Override
    public ProductDto getProductById(long productId) {
        log.trace("Инициировано получение товара по id");
        Product product = findEntityIn(productRepository, Product.class.getName(), productId);
        log.debug("Получен товар {} по productId {}", product, productId);
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(product.getCategory());
        return ProductMapper.toProductDto(product, categoryDto);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(long productId, UpdateProductRequest request) {
        log.trace("Инициировано обновление товара");
        Product product = findEntityIn(productRepository, Product.class.getName(), productId);

        Category category = null;

        if (request.categoryId() != null) {
            category = findEntityIn(categoryRepository, Category.class.getName(), request.categoryId());
        }

        updateProductFields(product, request, category);
        Product updatedProduct = productRepository.save(product);
        log.debug("Обновлен товар {}", updatedProduct);
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(updatedProduct.getCategory());
        return ProductMapper.toProductDto(updatedProduct, categoryDto);
    }

    @Override
    public List<ProductDto> searchProducts(String query) {
        log.trace("Инициирован поиск товаров");
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        log.debug("Получен список товаров {} по запросу {}", products, query);
        return convertToListProductDto(products);
    }

    @Override
    public List<ProductDto> getProductsByCategory(long categoryId) {
        log.trace("Инициировано получение списка товаров по категории");
        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.debug("Получен список товаров {} по categoryId {}", products, categoryId);
        return convertToListProductDto(products);
    }

    private void updateProductFields(Product product, UpdateProductRequest request, Category category) {
        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (category != null) {
            product.setCategory(category);
        }

        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        if (request.active() != null) {
            product.setActive(request.active());
        }
    }

    private List<ProductDto> convertToListProductDto(List<Product> products) {
        return products.stream()
                .map(product -> {
                    CategoryDto categoryDto = CategoryMapper.toCategoryDto(product.getCategory());
                    return ProductMapper.toProductDto(product, categoryDto);
                })
                .toList();
    }
}
