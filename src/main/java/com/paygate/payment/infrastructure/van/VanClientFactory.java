package com.paygate.payment.infrastructure.van;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * VAN 클라이언트 팩토리.
 * VAN 타입에 따라 적절한 클라이언트를 반환.
 */
@Component
@RequiredArgsConstructor
public class VanClientFactory {

    private final Map<String, VanClient> vanClientMap;

    public VanClientFactory(List<VanClient> vanClients) {
        this.vanClientMap = vanClients.stream()
                .collect(Collectors.toMap(
                        VanClient::getVanType,
                        Function.identity()
                ));
    }

    public VanClient getClient(String vanType) {
        VanClient client = vanClientMap.get(vanType);
        if (client == null) {
            // 기본 VAN으로 NICE 사용
            return vanClientMap.get("NICE");
        }
        return client;
    }

    public VanClient getDefaultClient() {
        return vanClientMap.get("NICE");
    }
}
