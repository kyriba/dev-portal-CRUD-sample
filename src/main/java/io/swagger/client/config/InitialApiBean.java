package io.swagger.client.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InitialApiBean {

    private final String apiName;

    private final ApiConfig apiConfig;

    public InitialApiBean(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        apiName = getName();
    }

    public String getApiName() {
        return apiName;
    }

    private String getName() {
        Scanner sc = new Scanner(System.in);
        List<String> apis = apiConfig.getApis();
        Map<String, String> list = apis.stream()
                .collect(Collectors.toMap(e -> String.valueOf(apis.indexOf(e) + 1), e -> e));
        String input;
        do {
            System.out.println(apis.stream()
                    .map(e -> e = apis.indexOf(e) + 1 + ". " + e)
                    .collect(Collectors.joining("\n", "\nPlease enter api name or order number from list: \n", "")));
            input = sc.nextLine();
            input = input.trim();
            if (input.matches("[0-9]+")) {
                if (!list.containsKey(input)) {
                    System.out.println("Api with order number " + input + " not found.");
                }
            } else if (!list.containsValue(input)) {
                System.out.println("Api with name " + input + " not found.");
            }
        }
        while (!list.containsKey(input) && !list.containsValue(input));
        return input.matches("[0-9]+") ? list.get(input) : input;
    }
}
