package ru.yandex.practicum.order.fallback;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.feign.ProductDto;

@Service
@RequiredArgsConstructor
public class ProductAdapter {
    private final ProductClient productClient;

    public RemoteCallResult<ProductDto> getProductById(Long id) {
        try {
            ProductDto product = productClient.getProductById(id);
            return new RemoteCallResult.Success<>(product);
        } catch (FeignException e) {
            return RemoteCallMethods.toFailureResult(e);
        } catch (Exception e) {
            return RemoteCallMethods.toFailureResult(e);
        }
    }
}
