package io.swagger.client.service.impl;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.Api;
import io.swagger.client.config.FieldsConfig;
import io.swagger.client.config.InitialApiBean;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.exception.InvalidTokenException;
import io.swagger.client.model.*;
import io.swagger.client.service.ApiService;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApiServiceImpl implements ApiService {

    @Value("${access.token.url}")
    public String ACCESS_TOKEN_URL;
    @Value("${client.id}")
    public String CLIENT_ID;
    @Value("${client.secret}")
    public String CLIENT_SECRET;

    private final Api api;

    private final InitialApiBean initialApiBean;

    private final FieldsConfig fieldsConfig;

    private StringBuilder requestBody;
    private List<String> apiFields;
    private List<String> codes = new ArrayList<>();
    private final List<String> createdCodes = new ArrayList<>();
    private Map<String, Set<String>> distinctAndSortedValuesOfFields;

    public ApiServiceImpl(Api api, InitialApiBean initialApiBean, FieldsConfig fieldsConfig) {
        this.api = api;
        this.initialApiBean = initialApiBean;
        this.fieldsConfig = fieldsConfig;
    }

    @PostConstruct
    private void authenticate() {
        refreshToken();
        this.apiFields = fieldsConfig.getFields().get(initialApiBean.getApiName().replaceAll("/", ""));
        System.out.println("Wait a minute");
        codes = findAllCodes();
        distinctAndSortedValuesOfFields = getAndSortDistinctValuesOfFields();
    }

    @Override
    public List<String> getAllCodes() {
        return codes;
    }

    @Override
    public String getRequestBody() {
        return String.valueOf(requestBody);
    }

    @Override
    public List<String> getCreatedCodes() {
        return createdCodes;
    }

    @Override
    public Map<String, Set<String>> getSortedDistinctValuesOfFields() {
        return distinctAndSortedValuesOfFields;
    }

    private void refreshToken() {

        try {
            OAuthClient client = new OAuthClient(new URLConnectionClient());

            OAuthClientRequest request =
                    OAuthClientRequest.tokenLocation(ACCESS_TOKEN_URL)
                            .setGrantType(GrantType.CLIENT_CREDENTIALS)
                            .setClientId(CLIENT_ID)
                            .setClientSecret(CLIENT_SECRET)
                            .buildQueryMessage();
            request.addHeader("Accept", "application/json");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "Basic " + base64EncodedBasicAuthentication());

            OAuthAccessTokenResponse token = client.accessToken(request, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);
            String accessToken = token.getAccessToken();
//            System.out.println(token.getBody() + "\n");
            ApiClient defaultClient = api.getApiClient();
            io.swagger.client.auth.OAuth OAuth2ClientCredentials = (io.swagger.client.auth.OAuth) defaultClient.getAuthentication("OAuth2ClientCredentials");
            OAuth2ClientCredentials.setAccessToken(accessToken);

        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }

    private String base64EncodedBasicAuthentication() {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public String getAll(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort) {
        String result;
        try {
            result = api.readAccountsUsingGET1(activeStatus, filter, pageLimit, pageOffset, sort);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public String getByCode(String code) {
        String result;
        try {
            result = api.readAccountUsingGET1(code);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no result with code " + code);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public String getByUuid(String uuid) {
        String result;
        try {
            result = api.readAccountUsingGET3(UUID.fromString(uuid));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no result with uuid " + uuid);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel create(Map<String, String> map) {
        requestBody = new StringBuilder("{");

        List<String[]> listOfFields = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>(map.values());

        for (String s : map.keySet()) {
            listOfFields.add(s.split("\\."));
        }

        listOfFields.add(new String[]{""});
        listOfValues.add("");

        requestBody = buildJsonRequestBody(requestBody, listOfFields,
                listOfValues, 0, 0, listOfValues.size());
        requestBody.deleteCharAt(requestBody.length() - 1).append("}");

        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.createUsingPOST1(String.valueOf(requestBody));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            codes.add(map.get("code").toUpperCase());
            createdCodes.add(map.get("code").toUpperCase());
            addNewDistinctValuesOfFields(map);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel update(Map<String, String> map) {

        requestBody = new StringBuilder("{");

        List<String[]> listOfFields = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>(map.values());

        for (String s : map.keySet()) {
            listOfFields.add(s.split("\\."));
        }

        listOfFields.add(new String[]{""});
        listOfValues.add("");

        requestBody = buildJsonRequestBody(requestBody, listOfFields,
                listOfValues, 0, 0, listOfValues.size());
        requestBody.deleteCharAt(requestBody.length() - 1).append("}");

        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.updateUsingPUT1(String.valueOf(requestBody), map.get("code"));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            addNewDistinctValuesOfFields(map);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel delete(String code) {

        if (!createdCodes.contains(code)) {
            throw new BadRequestException("There is no result with code " + code + " which can be delete");
        }

        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.deleteByCodeUsingDELETE1(code);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            createdCodes.remove(code);
            codes.remove(code);
        }
        return responseIdModel;
    }

    private List<String> findAllCodes() {
        String result = getAll(null, null, null, null, null);
        Pattern pattern = Pattern.compile("(?<=\\[\\{)(.+)(?=\\}\\])");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()){
            result = matcher.group().replaceAll(":\\{(.+?)\\}", "");
        }
        pattern = Pattern.compile("(?<=\"code\":\")(.+?)(?=\",)");
        matcher = pattern.matcher(result);
        while (matcher.find()){
            codes.add(matcher.group());
        }
        return codes;
    }

    private Map<String, Set<String>> getAndSortDistinctValuesOfFields() {
        Map<String, Set<String>> mapOfDistinctAndSortedValuesOfFields = new TreeMap<>();
        StringBuilder results = new StringBuilder();
        for (String code : codes) {
            results.append(getByCode(code));
        }

        AtomicInteger inc = new AtomicInteger();
        apiFields.stream().map(field -> field.split("\\."))
                .forEach(fieldPart -> mapOfDistinctAndSortedValuesOfFields.put(apiFields.get(inc.getAndIncrement()),
                        findDistinctAndSortedValuesOfFields(fieldPart, String.valueOf(results))));

        return mapOfDistinctAndSortedValuesOfFields;
    }

    private static Set<String> findDistinctAndSortedValuesOfFields(String[] fieldParts, String results) {
        Set<String> distinctAndSortedValuesOfField = new TreeSet<>();
        StringBuilder stringBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile(String.valueOf(buildRegex(fieldParts, stringBuilder, 0)));
        Matcher matcher = pattern.matcher(results);

        while (matcher.find()) {
            final String valueOfField = matcher.group(1);
            distinctAndSortedValuesOfField.add(valueOfField);
        }
        return distinctAndSortedValuesOfField;
    }

    private static StringBuilder buildRegex(String[] fieldParts, StringBuilder regexBuilder, int position) {
        String part1OfRegex1And2 = "(?<=\"";
        String part2OfRegex1 = "\":\\{)";
        String part3OfRegex1 = "(?:.*?)";
        String part4OfRegex1 = "(?=\\})";
        String part2OfRegex2 = "\":\")(.*?)(?:\")";
        if (fieldParts.length == 1) {
            return regexBuilder.append(part1OfRegex1And2).append(fieldParts[position]).append(part2OfRegex2);
        }
        if (position == fieldParts.length - 1) {
            return regexBuilder.append(part1OfRegex1And2).append(fieldParts[position]).append(part2OfRegex2);
        }
        regexBuilder.append(part1OfRegex1And2).append(fieldParts[position]).append(part2OfRegex1).append(part3OfRegex1);
        regexBuilder = buildRegex(fieldParts, regexBuilder, position + 1);
        regexBuilder.append(part3OfRegex1).append(part4OfRegex1);
        return regexBuilder;
    }

    private static StringBuilder buildJsonRequestBody(StringBuilder requestBody, List<String[]> listOfFields,
                                                      List<String> listOfValues, int position, int loopStart, int loopEnd) {
        String curlyBracket1 = "{";
        String curlyBracket2 = "}";
        String quotes = "\"";
        String fieldEnd = "\": ";
        String comma = ",";

        for (int i = loopStart; i < listOfFields.size() - 1
                && position < listOfFields.get(i).length && i <= loopEnd; i++) {

            if (position < listOfFields.get(i + 1).length &&
                    compareFields(listOfFields.get(i), listOfFields.get(i + 1), position)) {
                continue;
            }
            requestBody.append(quotes).append(listOfFields.get(i)[position]).append(fieldEnd);
            if (position == listOfFields.get(loopStart).length - 1) {
                requestBody.append(quotes).append(listOfValues.get(loopStart)).append(quotes);
            } else {
                requestBody.append(curlyBracket1);
            }
            requestBody = buildJsonRequestBody(requestBody, listOfFields, listOfValues, position + 1, loopStart, i);
            if (position > listOfFields.get(loopStart + 1).length - 1 || loopStart == loopEnd) {
                requestBody.append(curlyBracket2);
            } else if (position <= listOfFields.get(loopStart + 1).length - 1) {
                requestBody.append(comma);
            }
            loopStart = i + 1;
        }

        return requestBody;
    }

    private static boolean compareFields(String[] fields1, String[] fields2, int position) {
        for (int i = 0; i <= position; i++) {
            if (!(fields1[i].equals(fields2[i])))
                return false;
        }
        return true;
    }

    private void addNewDistinctValuesOfFields(Map<String, String> map) {
        Iterator<String> iterator = map.keySet().iterator();
        String key;
        while (iterator.hasNext()) {
            key = iterator.next();
            distinctAndSortedValuesOfFields.get(key).add(map.get(key));
        }
    }
}