package ru.yandex.practicum.product.service;

import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {
    List<ProductDto> getAllActiveProducts();

    ProductDto createProduct(CreateProductRequest request);

    ProductDto getProductById(long productId);

    ProductDto updateProduct(long productId, UpdateProductRequest request);

    List<ProductDto> searchProducts(String query);

    List<ProductDto> getProductsByCategory(long categoryId);
}
