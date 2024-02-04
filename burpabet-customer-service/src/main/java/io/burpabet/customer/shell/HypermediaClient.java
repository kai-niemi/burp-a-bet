package io.burpabet.customer.shell;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

@Component
public class HypermediaClient {
    public static final TypeReferences.CollectionModelType<Map<String, Object>> MAP_MODEL_TYPE
            = new TypeReferences.CollectionModelType<>() {
    };

    @Value("${burp.wallet-api-url}")
    private String walletApiUri;

    public <R> R traverseWalletApi(Function<Traverson, R> consumer) {
        Traverson traverson = new Traverson(URI.create(walletApiUri), MediaTypes.HAL_JSON);
        return consumer.apply(traverson);
    }
}
