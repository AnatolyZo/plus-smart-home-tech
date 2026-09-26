package ru.yandex.practicum.order.fallback;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ReleaseRequest;
import ru.yandex.practicum.order.feign.ReserveRequest;

@Service
@RequiredArgsConstructor
public class InventoryAdapter implements Retryable {
    private final InventoryClient inventoryClient;

    public RemoteCallResult<Void> reserveStock(ReserveRequest request) {
        int attemptsToFindAliveServer = 5;

        for (int attempt = 1; attempt <= attemptsToFindAliveServer; attempt++) {
            try {
                inventoryClient.reserveStock(request);
                return new RemoteCallResult.Success<>(null);
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

    public RemoteCallResult<Void> releaseStock(ReleaseRequest request) {
        try {
            inventoryClient.releaseStock(request);
            return new RemoteCallResult.Success<>(null);
        } catch (FeignException e) {
            return RemoteCallMethods.toFailureResult(e);
        } catch (Exception e) {
            return RemoteCallMethods.toFailureResult(e);
        }
    }
}
