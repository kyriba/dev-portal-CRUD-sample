package io.swagger.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties("enums.available")
public class AvailableValuesConfig {

    private Map<String, Map<String, List<String>>> values;

    public Map<String, Map<String, List<String>>> getValues() {
        return values;
    }

    public void setValues(Map<String, Map<String, List<String>>> values) {
        this.values = values;
    }
}

