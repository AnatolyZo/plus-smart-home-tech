package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

import static ru.yandex.practicum.product.controller.ControllerConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(URL_API + URL_PRODUCTS)
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getAllActiveProducts() {
        return productService.getAllActiveProducts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{" + ID + "}")
    public ProductDto getProductById(@PathVariable(name = ID) long productId) {
        return productService.getProductById(productId);
    }

    @PatchMapping("/{" + ID + "}")
    public ProductDto updateProduct(@PathVariable(name = ID) long productId, @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(productId, request);
    }

    @GetMapping(URL_SEARCH)
    public List<ProductDto> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    @GetMapping(URL_CATEGORY + "/{" + ID_CATEGORY + "}")
    public List<ProductDto> getProductsByCategory(@PathVariable long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }
}
