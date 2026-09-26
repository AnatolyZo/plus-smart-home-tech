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
        int attemptsToFindAliveServer = 10;

        for (int attempt = 1; attempt <= attemptsToFindAliveServer; attempt++) {
            try {
                ProductDto product = productClient.getProductById(id);
                return new RemoteCallResult.Success<>(product);
            } catch (FeignException e) {
                if (attempt < attemptsToFindAliveServer && isRetryable(e)) {
                    continue;
                }
                return RemoteCallMethods.toFailureResult(e);
            } catch (Exception e) {
                return RemoteCallMethods.toFailureResult(e);
            }
        }

        throw new IllegalStateException("Ошибка логики работы программы");
    }

    private boolean isRetryable(FeignException e) {
        int status = e.status();
        return status >= 500 || status == -1;
    }
}
