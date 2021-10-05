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
import org.json.JSONArray;
import org.json.JSONObject;
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

    private final String[] accountsFieldsToExclude = new String[]{"status", "includeInElectronicBank",
            "includeInElectronicBankDate", "excludeFromElectronicBank", "excludeFromElectronicBankDate",
            "confirmAccountList", "confirmAccountListDate", "confirmAccountBalances", "confirmAccountBalancesDate",
            "confirmAccountSignatories", "confirmAccountSignatoriesDate", "accountOpeningRequested",
            "accountOpeningRequestDate", "accountClosureRequested", "accountClosureRequestDate"};
    //    private final String[] cashFlowsFieldsToExclude = new String[]{"flowDescription", "glStatus", "number", "origin",
//            "status"};
//    private final String[] cashFlowsFieldsToInclude = new String[]{"description", "flowStatus"};
    private final String[] thirdPartiesFieldsToExclude = new String[]{"internalCounter", "internalCounterSuffix"};

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
        if (apiMethods.contains("GET_ALL"))
            codes = findAllIdentifiers(getAll(null, null, 1000, null, null), "code");
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

    @Override
    public List<String> getApiFields() {
        return apiFields;
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

        requestBody = buildPOSTJsonRequestBody(requestBody, listOfFields,
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

        List<String> fieldsToExclude;
//        List<String> fieldsToInclude;
        switch (initialApiBean.getApiName()) {
            case "/v1/accounts":
                fieldsToExclude = new ArrayList<>(Arrays.asList(accountsFieldsToExclude));
//                fieldsToInclude = new ArrayList<>();
                break;
//            case "/v1/cash-flows":
//                fieldsToExclude = new ArrayList<>(Arrays.asList(cashFlowsFieldsToExclude));
//                fieldsToInclude = new ArrayList<>(Arrays.asList(cashFlowsFieldsToInclude));
//                break;
            case "/v1/third-parties":
                fieldsToExclude = new ArrayList<>(Arrays.asList(thirdPartiesFieldsToExclude));
//                fieldsToInclude = new ArrayList<>();
                break;
            default:
                fieldsToExclude = new ArrayList<>();
//                fieldsToInclude = new ArrayList<>();
                break;
        }

        if (apiMethods.contains("GET_BY_CODE")) {
            requestBody = new StringBuilder(getByCode(item.get("code")));
        } else {
            requestBody = new StringBuilder(getByUuid(item.get("uuid")));
        }

        List<String[]> listOfFields = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>(item.values());

        for (String s : item.keySet()) {
            if (apiFields.contains(s) && !s.equals("code") && !s.equals("uuid"))
                listOfFields.add(s.split("\\."));
            else
                listOfValues.remove(item.get(s));
        }

        for (int i = 0; i < listOfFields.size(); i++) {
            requestBody = new StringBuilder(buildPUTJsonRequestBody(String.valueOf(requestBody),
                    listOfFields.get(i), listOfValues.get(i), fieldsToExclude));
        }

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
                        findDistinctAndSortedValuesOfFields(fieldPart, result)));

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
                        findDistinctAndSortedValuesOfFields(fieldPart, result)));

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
        pattern = Pattern.compile("(?<=\"" + identifier + "\":\")(.+?)(?=\")");
        matcher = pattern.matcher(result);
        while (matcher.find()) {
            codesList.add(matcher.group());
        }
        return codesList;
    }

    private Map<String, Set<String>> getAndSortDistinctValuesOfFields() {
        Map<String, Set<String>> mapOfDistinctAndSortedValuesOfFields = new TreeMap<>();
        StringBuilder results = new StringBuilder();
        if (apiMethods.contains("GET_BY_CODE") && !initialApiBean.getApiName().equals("/v1/data-permissions")) {
            for (String code : codes) {
                results.append(getByCode(code));
            }
        } else if (apiMethods.contains("GET_BY_UUID") && apiMethods.contains("GET_ALL")
                && !initialApiBean.getApiName().equals("/v1/data-permissions")) {
            List<String> uuids =
                    findAllIdentifiers(getAll(null, null, 1000, null, null),
                            "uuid");
            for (String uuid : uuids) {
                results.append(getByUuid(uuid));
            }
        } else {
            results.append(getAll(null, null, 1000, null, null)
                    .replace("\"results\":[", ""));
        }

        AtomicInteger inc = new AtomicInteger();
        apiFields.stream().map(field -> field.split("\\."))
                .forEach(fieldPart -> mapOfDistinctAndSortedValuesOfFields.put(apiFields.get(inc.getAndIncrement()),
                        findDistinctAndSortedValuesOfFields(fieldPart, String.valueOf(results))));

        return mapOfDistinctAndSortedValuesOfFields;
    }

    private Set<String> findDistinctAndSortedValuesOfFields(String[] fieldParts, String results) {
        if (fieldParts.length == 1) {
            results = results.replaceAll(":\\{(.+?)\\}", "");
        }
        Set<String> distinctAndSortedValuesOfField = new TreeSet<>();
        int position = 0;
        boolean isArrayField = false;

        for (int i = 0; i < fieldParts.length; i++) {
            if (fieldParts[i].contains("[]")) {
                position = i + 1;
                isArrayField = true;
                break;
            }
            if (i == fieldParts.length - 1)
                results = results.replaceAll(":\\[(.*?)\\]", "");
        }

        Pattern pattern = Pattern.compile(String.valueOf(buildRegex(fieldParts, new StringBuilder(), 0)));
        Matcher matcher = pattern.matcher(results);

        if (position == fieldParts.length || !isArrayField) {
            while (matcher.find()) {
                distinctAndSortedValuesOfField.add(matcher.group(1));
            }
            return distinctAndSortedValuesOfField;
        }
        Pattern endPattern;
        Matcher endMatcher;
        while (matcher.find()) {
            StringBuilder stringBuilder = new StringBuilder();
            endPattern = Pattern.compile(String.valueOf(buildRegex(fieldParts, new StringBuilder(), position)));
            endMatcher = endPattern.matcher(matcher.group(1));
            while (endMatcher.find()) {
                stringBuilder.append(endMatcher.group(1)).append(", ");
            }
            distinctAndSortedValuesOfField.add(stringBuilder.delete(stringBuilder.length() - 2,
                    stringBuilder.length()).toString());
        }

        return distinctAndSortedValuesOfField;
    }

    private StringBuilder buildRegex(String[] fieldParts, StringBuilder regexBuilder, int position) {
        String part1OfRegex1And2 = "(?<=\"";
        String part2OfRegex1 = "\":\\{)";
        String part3OfRegex1 = "(?:.*?)";
        String part4OfRegex1 = "(?=\\})";
        String part2OfRegex2 = "\":\")(.*?)(?:\")";
        String arrayRegex = "\":\\[)(.*?)(?=\\])";
        if (fieldParts[position].contains("[]")) {
            return regexBuilder.append(part1OfRegex1And2).append(fieldParts[position].replace("[]", ""))
                    .append(arrayRegex);
        }
        if (fieldParts.length == 1 || position == fieldParts.length - 1) {
            return regexBuilder.append(part1OfRegex1And2).append(fieldParts[position]).append(part2OfRegex2);
        }
        regexBuilder.append(part1OfRegex1And2).append(fieldParts[position]).append(part2OfRegex1).append(part3OfRegex1);
        regexBuilder = buildRegex(fieldParts, regexBuilder, position + 1);
        regexBuilder.append(part3OfRegex1).append(part4OfRegex1);
        return regexBuilder;
    }

    private String buildPUTJsonRequestBody(String requestBody, String[] fields, String value,
                                           List<String> fieldsToExclude) {
        JSONObject jsonObject = new JSONObject(requestBody);
        for (int i = 0; i < fieldsToExclude.size(); i++) {
            if (fieldsToExclude.get(i).contains("[]")) {
                jsonObject.remove(fieldsToExclude.remove(i--).replace("[]", ""));
            } else {
                jsonObject.remove(fieldsToExclude.remove(i--));
            }
        }
        JSONObject copyOfJsonObject = jsonObject;
        for (int j = 0; j < fields.length - 1; j++) {
            if (fields[j].contains("[]")) {
                JSONArray jsonArray;
                if (!copyOfJsonObject.isNull(fields[j].replace("[]", ""))) {
                    jsonArray = copyOfJsonObject.getJSONArray(fields[j].replace("[]", ""));
                } else {
                    jsonArray = new JSONArray();
                }
                jsonArray = new JSONArray(buildPUTJsonArray(jsonArray.toString(),
                        Arrays.stream(fields).skip(j + 1).toArray(String[]::new), value.split(",")));
                copyOfJsonObject.put(fields[j].replace("[]", ""), jsonArray);
                return jsonObject.toString();
            } else {
                if (copyOfJsonObject.isNull(fields[j])) {
                    copyOfJsonObject = copyOfJsonObject.put(fields[j], new JSONObject());
                }
                copyOfJsonObject = copyOfJsonObject.getJSONObject(fields[j]);
            }
        }
        String lastFieldPart = fields[fields.length - 1].replace("[]", "");
        if (lastFieldPart.equals("code")) {
            copyOfJsonObject.put(lastFieldPart, value);
            if (copyOfJsonObject.has("uuid")) {
                copyOfJsonObject.remove("uuid");
            }
        } else if (lastFieldPart.equals("uuid")) {
            copyOfJsonObject.put(lastFieldPart, value);
            if (copyOfJsonObject.has("code")) {
                copyOfJsonObject.remove("code");
            }
        } else {
            copyOfJsonObject.put(lastFieldPart, value);
        }
        return jsonObject.toString();
    }

    private String buildPUTJsonArray(String arrayBody, String[] fields, String[] values) {
        JSONArray jsonArray = new JSONArray(arrayBody);
        JSONArray newJsonArray = new JSONArray();
        for (String value : values) {
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                for (int k = 0; k < fields.length - 1; k++) {
                    if (jsonObject.isNull(fields[k])) {
                        jsonObject.put(fields[k], new JSONObject());
                    }
                    jsonObject = jsonObject.getJSONObject(fields[k]);
                }
                String lastFieldPart = fields[fields.length - 1];
                if (!jsonObject.isNull(lastFieldPart) && jsonObject.get(lastFieldPart).equals(value.trim())) {
                    if ((lastFieldPart.equals("code") || lastFieldPart.equals("uuid"))) {
                        newJsonArray.put(jsonArray.getJSONObject(j));
                    } else {
                        newJsonArray.put(new JSONObject().put(lastFieldPart, value.trim()));
                    }
                    break;
                } else if (j == jsonArray.length() - 1) {
                    newJsonArray.put(new JSONObject(jsonObject).put(lastFieldPart, value.trim()));
                }
            }
        }
        return newJsonArray.toString();
    }

    private StringBuilder buildPOSTJsonRequestBody(StringBuilder requestBody, List<String[]> listOfFields,
                                                   List<String> listOfValues, int position, int loopStart, int loopEnd) {
        String arrayStart = "[";
        String arrayEnd = "]";
        String curlyBracket1 = "{";
        String curlyBracket2 = "}";
        String quotes = "\"";
        String fieldEnd = "\": ";
        String comma = ",";
        boolean isArrayField = false;

        for (int i = loopStart; i < listOfFields.size() - 1
                && position < listOfFields.get(i).length && i <= loopEnd; i++) {

            if (position < listOfFields.get(i + 1).length &&
                    compareFields(listOfFields.get(i), listOfFields.get(i + 1), position)) {
                continue;
            }
            if (listOfFields.get(i)[position].contains("[]")) {
                listOfFields.get(i)[position] = listOfFields.get(i)[position].replace("[]", "");
                isArrayField = true;
            }
            requestBody.append(quotes).append(listOfFields.get(i)[position]).append(fieldEnd);
            if (isArrayField) {
                requestBody.append(arrayStart);
                requestBody = buildPOSTJsonArray(loopStart, i, position + 1, listOfFields,
                        listOfValues, requestBody);
                loopStart = i;
                requestBody.deleteCharAt(requestBody.length() - 1).append(arrayEnd);
            }
            if (position == listOfFields.get(loopStart).length - 1 && !isArrayField) {
                requestBody.append(quotes).append(listOfValues.get(loopStart)).append(quotes);
            } else if (position != listOfFields.get(loopStart).length - 1 && !isArrayField) {
                requestBody.append(curlyBracket1);
            }
            if (!isArrayField) {
                requestBody = buildPOSTJsonRequestBody(requestBody, listOfFields, listOfValues,
                        position + 1, loopStart, i);
            }
            isArrayField = false;
            if (position > listOfFields.get(i + 1).length - 1 || i == loopEnd
                    || (containsArrayField(listOfFields.get(loopStart)) && position != 0)) {
                requestBody.append(curlyBracket2);
            } else if (position <= listOfFields.get(i + 1).length - 1) {
                requestBody.append(comma);
            }
            loopStart = i + 1;
        }

        return requestBody;
    }

    private StringBuilder buildPOSTJsonArray(int start, int end, int position, List<String[]> listOfFields,
                                             List<String> listOfValues, StringBuilder requestBody) {
        List<List<String>> listOfArrayValues = new ArrayList<>();
        List<String[]> arrayFields = new ArrayList<>();
        if (position >= listOfFields.get(start).length) {
            requestBody.append(listOfValues.get(start)).append(",");
            return requestBody;
        }
        String[] subArrayOfFields;
        List<String[]> allArrayValues = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            allArrayValues.add(listOfValues.get(i).split(","));
            subArrayOfFields = new String[listOfFields.get(i).length - position];
            System.arraycopy(listOfFields.get(i), position, subArrayOfFields, 0,
                    listOfFields.get(i).length - position);
            arrayFields.add(subArrayOfFields);
        }
        int maxLengthOfArrayValues = 0;
        for (String[] arrayValues : allArrayValues) {
            if (arrayValues.length > maxLengthOfArrayValues)
                maxLengthOfArrayValues = arrayValues.length;
        }
        for (int i = 0; i < maxLengthOfArrayValues; i++) {
            List<String> setOfValues = new ArrayList<>();
            for (String[] arrayValues : allArrayValues) {
                if (i < arrayValues.length) {
                    setOfValues.add(arrayValues[i].trim());
                } else {
                    setOfValues.add("");
                }
            }
            setOfValues.add("");
            listOfArrayValues.add(setOfValues);
        }
        arrayFields.add(new String[]{""});
        for (List<String> listOfArrayValue : listOfArrayValues) {
            requestBody.append("{");
            requestBody = buildPOSTJsonRequestBody(requestBody, arrayFields, listOfArrayValue,
                    0, 0, arrayFields.size());
            requestBody.deleteCharAt(requestBody.length() - 1).append("},");
        }
        return requestBody;
    }

    private boolean containsArrayField(String[] fields) {
        if (fields.length == 1)
            return false;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].contains("[]") && i != fields.length - 1)
                return true;
        }
        return false;
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