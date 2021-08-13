package io.swagger.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties("api")
public class MethodsConfig {

    private Map<String, List<String>> methods;

    public Map<String, List<String>> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, List<String>> methods) {
        this.methods = methods;
    }
}
