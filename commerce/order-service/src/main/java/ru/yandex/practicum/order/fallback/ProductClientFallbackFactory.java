package ru.yandex.practicum.order.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.feign.ProductDto;

@Component
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {

            @Override
            public ProductDto getProductById(Long productId) {
                log.warn(
                        "product-service недоступен при запросе товара id={}",
                        productId,
                        cause
                );

                throw new ProductServiceUnavailableException(productId, cause);
            }

        };
    }
}
