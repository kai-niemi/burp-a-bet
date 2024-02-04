package io.burpabet.betting;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = BettingApplication.class)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Order(InteractiveShellRunner.PRECEDENCE - 100)
public class BettingApplication implements PromptProvider {
    public static void main(String[] args) {
        new SpringApplicationBuilder(BettingApplication.class)
                .logStartupInfo(false)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("betting:$ ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
