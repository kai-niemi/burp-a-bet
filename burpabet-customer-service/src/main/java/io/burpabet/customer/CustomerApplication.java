package io.burpabet.customer;

import java.util.Arrays;
import java.util.LinkedList;

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
@ConfigurationPropertiesScan(basePackageClasses = CustomerApplication.class)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Order(InteractiveShellRunner.PRECEDENCE - 100)
public class CustomerApplication implements PromptProvider {
    public static void main(String[] args) {
        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        LinkedList<String> passThroughArgs = new LinkedList<>();

        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.startsWith("--")) {
                if (arg.equals("--noshell")) {
                    System.setProperty("spring.shell.interactive.enabled", "false");
                } else {
                    passThroughArgs.add(arg);
                }
            } else {
                passThroughArgs.add(arg);
            }
        }

        new SpringApplicationBuilder(CustomerApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.SERVLET)
                .run(passThroughArgs.toArray(new String[] {}));
    }

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("customer:$ ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
