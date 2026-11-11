package com.kg.macroanalyzer.scrape.adaptors.http;

import com.kg.macroanalyzer.scrape.application.port.in.InPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * A service without its own prefix has no route on the gateway, so it must fail at start-up
 * rather than answer elsewhere.
 */
class PathPrefixRequiredTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    PropertyPlaceholderAutoConfiguration.class,
                    WebMvcAutoConfiguration.class))
            .withBean(InPort.class, () -> mock(InPort.class))
            .withUserConfiguration(InsertionAdaptor.class);

    @Test
    void shouldFailToStart_withoutAPathPrefix() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .rootCause()
                    .hasMessageContaining("macro.scrape.path-prefix");
        });
    }

    @Test
    void shouldStart_withAPathPrefix() {
        runner.withPropertyValues("macro.scrape.path-prefix=macro-analyzer/scrape/some-service")
                .run(context -> assertThat(context).hasNotFailed());
    }

}
