package io.burpabet.betting.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@Profile(value = {"dev"})
public class ThymeleafConfiguration {
    @Autowired
    private ThymeleafProperties properties;

    @Bean
    public ITemplateResolver defaultTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(properties.getSuffix());
        resolver.setPrefix("burpabet-betting-service/src/main/resources/templates/");
        resolver.setTemplateMode(properties.getMode());
        resolver.setCharacterEncoding(properties.getEncoding().name());
        resolver.setCacheable(false);
        resolver.setOrder(0);
        return resolver;
    }
}
