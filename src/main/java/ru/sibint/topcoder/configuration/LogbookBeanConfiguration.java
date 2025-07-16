package ru.sibint.topcoder.configuration;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.json.JsonHttpLogFormatter;

@Configuration
@AllArgsConstructor
public class LogbookBeanConfiguration {

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .condition(httpRequest ->
                        !(httpRequest.getPath().contains("/actuator") ||
                                httpRequest.getPath().contains("/swagger-ui") ||
                                httpRequest.getPath().contains("/swagger-resources"))
                )
                .sink(new DefaultSink(new JsonHttpLogFormatter(), new DefaultHttpLogWriter()))
                .build();
    }

}