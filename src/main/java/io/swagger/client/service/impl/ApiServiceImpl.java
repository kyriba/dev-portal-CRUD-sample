package io.swagger.client.service.impl;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ResponseIdModel;
import io.swagger.client.api.Api;
import io.swagger.client.config.AvailableValuesConfig;
import io.swagger.client.config.FieldsConfig;
import io.swagger.client.config.InitialApiBean;
import io.swagger.client.config.MethodsConfig;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.exception.InvalidTokenException;
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
    private final MethodsConfig methodsConfig;
    private final AvailableValuesConfig availableValuesConfig;

    private StringBuilder requestBody;
    private List<String> apiFields;
    private List<String> apiMethods;
    private List<String> codes = new ArrayList<>();
    private final List<String> createdCodes = new ArrayList<>();
    private Map<String, Set<String>> distinctAndSortedValuesOfFields;
    private Map<String, List<String>> availableValues;

    public ApiServiceImpl(Api api, InitialApiBean initialApiBean, FieldsConfig fieldsConfig,
                          MethodsConfig methodsConfig, AvailableValuesConfig availableValuesConfig) {
        this.api = api;
        this.initialApiBean = initialApiBean;
        this.fieldsConfig = fieldsConfig;
        this.methodsConfig = methodsConfig;
        this.availableValuesConfig = availableValuesConfig;
    }

    @PostConstruct
    private void postConstruct() {
        refreshToken();
        this.apiFields = fieldsConfig.getFields().get(initialApiBean.getApiName().replaceAll("/", ""));
        availableValues = availableValuesConfig.getValues().get(initialApiBean.getApiName().replaceAll("/", ""));
        apiMethods = methodsConfig.getMethods().get(initialApiBean.getApiName().replaceAll("/", ""));
        if (availableValues == null)
            availableValues = new TreeMap<>();
        System.out.println("Wait a minute");
        codes = findAllIdentifiers(getAll(null, null, null, null, null), "code");
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

    @Override
    public Map<String, List<String>> getAvailableValues() {
        return availableValues;
    }

    @Override
    public List<String> getApiMethods() {
        return apiMethods;
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
            result = api.readItemsUsingGET1(activeStatus, filter, pageLimit, pageOffset, sort);
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
            result = api.readItemUsingGET1(code);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no item with code " + code);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public String getByUuid(String uuid) {
        String result;
        try {
            result = api.readItemUsingGET3(UUID.fromString(uuid));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no item with uuid " + uuid);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel create(Map<String, String> item) {
        requestBody = new StringBuilder("{");

        List<String[]> listOfFields = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>(item.values());

        for (String s : item.keySet()) {
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
            codes.add(item.get("code").toUpperCase());
            createdCodes.add(item.get("code").toUpperCase());
            addNewDistinctValuesOfFields(item);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel update(Map<String, String> item) {

        requestBody = new StringBuilder("{");

        List<String[]> listOfFields = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>(item.values());

        for (String s : item.keySet()) {
            if (apiFields.contains(s))
                listOfFields.add(s.split("\\."));
            else
                listOfValues.remove(item.get(s));
        }

        listOfFields.add(new String[]{""});
        listOfValues.add("");

        requestBody = buildJsonRequestBody(requestBody, listOfFields,
                listOfValues, 0, 0, listOfValues.size());
        requestBody.deleteCharAt(requestBody.length() - 1).append("}");

        ResponseIdModel responseIdModel;
        try {
            if (apiMethods.contains("PUT_BY_CODE")) {
                responseIdModel = api.updateUsingPUT1(String.valueOf(requestBody), item.get("code"));
            } else {
                responseIdModel = api.updateUsingPUT3(String.valueOf(requestBody), UUID.fromString(item.get("uuid")));
            }
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            addNewDistinctValuesOfFields(item);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel deleteByCode(String code) {

        if (!createdCodes.contains(code)) {
            throw new BadRequestException("There is no item with code " + code + " which can be delete");
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

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel deleteByUuid(String uuid) {

        String code = findAllIdentifiers(getByUuid(uuid), "code").get(0);
        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.deleteUsingDELETE1(UUID.fromString(uuid));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no item with uuid " + uuid + " which can be delete");
        }
        if (responseIdModel != null) {
            if (code != null) {
                createdCodes.remove(code);
                codes.remove(code);
            }
        }
        return responseIdModel;
    }

    @Override
    public Map<String, Set<String>> getByCodeToUpdate(String code) {

        final String CODE = "code";

        if (!distinctAndSortedValuesOfFields.containsKey(CODE)) {
            distinctAndSortedValuesOfFields.put(CODE, new TreeSet<>());
        }

        Map<String, Set<String>> updateBody = new TreeMap<>();
        String result = getByCode(code);

        AtomicInteger inc = new AtomicInteger();
        apiFields.stream().map(field -> field.split("\\."))
                .forEach(fieldPart -> updateBody.put(apiFields.get(inc.getAndIncrement()),
                        findDistinctAndSortedValuesOfFields(fieldPart, String.valueOf(result))));

        updateBody.put(CODE, Collections.singleton(code));

        return updateBody;
    }

    @Override
    public Map<String, Set<String>> getByUuidToUpdate(String uuid) {

        final String UUID = "uuid";

        if (!distinctAndSortedValuesOfFields.containsKey(UUID)) {
            distinctAndSortedValuesOfFields.put(UUID, new TreeSet<>());
        }

        Map<String, Set<String>> updateBody = new TreeMap<>();
        String result;
        try {
            result = getByUuid(uuid);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage() + " which can be update");
        }

        AtomicInteger inc = new AtomicInteger();
        apiFields.stream().map(field -> field.split("\\."))
                .forEach(fieldPart -> updateBody.put(apiFields.get(inc.getAndIncrement()),
                        findDistinctAndSortedValuesOfFields(fieldPart, String.valueOf(result))));

        updateBody.put(UUID, Collections.singleton(uuid));

        return updateBody;
    }

    private List<String> findAllIdentifiers(String result, String identifier) {
        List<String> codesList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=\"results\":\\[\\{)(.+)(?=\\}\\])");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            result = matcher.group();
        }
        result = result.replaceAll(":\\{(.+?)\\}", "");
        pattern = Pattern.compile("(?<=\"" + identifier + "\":\")(.+?)(?=\",)");
        matcher = pattern.matcher(result);
        while (matcher.find()) {
            codesList.add(matcher.group());
        }
        return codesList;
    }

    private Map<String, Set<String>> getAndSortDistinctValuesOfFields() {
        Map<String, Set<String>> mapOfDistinctAndSortedValuesOfFields = new TreeMap<>();
        StringBuilder results = new StringBuilder();
        if (apiMethods.contains("GET_BY_CODE")) {
            for (String code : codes) {
                results.append(getByCode(code));
            }
        } else if (apiMethods.contains("GET_BY_UUID")) {
            List<String> uuids =
                    findAllIdentifiers(getAll(null, null, null, null, null),
                            "uuid");
            for (String uuid : uuids) {
                results.append(getByUuid(uuid));
            }
        } else {
            results.append(getAll(null, null, null, null, null));
        }

        AtomicInteger inc = new AtomicInteger();
        apiFields.stream().map(field -> field.split("\\."))
                .forEach(fieldPart -> mapOfDistinctAndSortedValuesOfFields.put(apiFields.get(inc.getAndIncrement()),
                        findDistinctAndSortedValuesOfFields(fieldPart, String.valueOf(results))));

        return mapOfDistinctAndSortedValuesOfFields;
    }

    private Set<String> findDistinctAndSortedValuesOfFields(String[] fieldParts, String results) {
        if (fieldParts.length == 1)
            results = results.replaceAll(":\\{(.+?)\\}", "");
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

    private StringBuilder buildRegex(String[] fieldParts, StringBuilder regexBuilder, int position) {
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

    private StringBuilder buildJsonRequestBody(StringBuilder requestBody, List<String[]> listOfFields,
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

    private boolean compareFields(String[] fields1, String[] fields2, int position) {
        for (int i = 0; i <= position; i++) {
            if (!(fields1[i].equals(fields2[i])))
                return false;
        }
        return true;
    }

    private void addNewDistinctValuesOfFields(Map<String, String> item) {
        Iterator<String> iterator = item.keySet().iterator();
        String key;
        while (iterator.hasNext()) {
            key = iterator.next();
            if (key.equals("code"))
                distinctAndSortedValuesOfFields.get(key).add(item.get(key).toUpperCase());
            else
                distinctAndSortedValuesOfFields.get(key).add(item.get(key));
        }
    }
}