package io.burpabet.betting.shell;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

@Component
public class HypermediaClient {
    @Value("${burp.customer-api-url}")
    private String customerApiUri;

    public static final TypeReferences.CollectionModelType<Map<String, Object>> MAP_MODEL_TYPE
            = new TypeReferences.CollectionModelType<>() {
    };

    public static final ParameterizedTypeReference<PagedModel<Map<String, Object>>> PAGED_MODEL_TYPE
            = new ParameterizedTypeReference<>() {
    };

    public <R> R traverseCustomerApi(Function<Traverson, R> consumer) {
        Traverson traverson = new Traverson(URI.create(customerApiUri), MediaTypes.HAL_JSON);
        return consumer.apply(traverson);
    }
}
