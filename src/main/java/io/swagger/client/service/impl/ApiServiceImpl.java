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

    private List<String> apiFields;
    private List<AccountCRUD> accounts = new ArrayList<>();
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
        distinctAndSortedValuesOfFields = getAndSortDistinctValuesOfFields();
    }

    @Override
    public List<AccountCRUD> getCreated() {
        return accounts;
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
    public ResponseIdModel create(Map<String, String> account) {
        String accountDto = "{\n" +
                "    \"address\": {\n" +
                "        \"country\": {\n" +
                "            \"code\": \"US\"\n" +
                "        },\n" +
                "        \"city\": \"San Diego\"\n" +
                "    },\n" +
                "    \"bankAccountID\": {\n" +
                "        \"value\": \"BF1030134020015400945000643\",\n" +
                "        \"banStructure\": \"BBAN_STRUCTURE\"\n" +
                "    },\n" +
                "    \"branch\": {\n" +
                "        \"code\": \"USBANK01\"\n" +
                "    },\n" +
                "    \"calendar\": {\n" +
                "        \"code\": \"FR\"\n" +
                "    },\n" +
                "    \"code\": \"VITALII\",\n" +
                "    \"company\": {\n" +
                "        \"code\": \"COMPANY03\"\n" +
                "    },\n" +
                "    \"currency\": {\n" +
                "        \"code\": \"USD\"\n" +
                "    },\n" +
                "    \"timeZone\": \"GMT\"\n" +
                "}";

        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.createUsingPOST1(accountDto);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
//            accountDtoCRUD.setUuid(responseIdModel.getUuid());
//            accounts.add(accountDtoCRUD);
//            checkToAdd(account);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel update(Map<String, String> account) {

        AccountCRUD accountDtoCRUD = new AccountCRUD();
        Account accountDto = new Account();

        fillAccountCRUD(accountDtoCRUD, account, "PUT");
        fillAccount(accountDto, account, "PUT");

        ResponseIdModel responseIdModel;
        try {
            responseIdModel = api.updateUsingPUT1(accountDto, account.get("code"));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            removeByCode(account.get("code"));
            accounts.add(accountDtoCRUD);
            checkToAdd(account);
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel delete(String code) {
        Optional<AccountCRUD> createdAccount = accounts.stream()
                .filter(account1 -> code.equals(account1.getCode()))
                .findFirst();
        if (!createdAccount.isPresent()) {
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
            removeByCode(code);
        }
        return responseIdModel;
    }

    private void removeByCode(String code) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getCode().equals(code)) {
                accounts.remove(i);
                return;
            }
        }
    }

    @Override
    public List<String> getAllCodes() {
        List<String> codes = new ArrayList<>();
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
        Map<String, Set<String>> mapOfDistinctAndSortedValuesOfFields = new HashMap<>();
        List<String> codes = getAllCodes();
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

    private void checkToAdd(Map<String, String> account) {
        List<String> mapKeys = Arrays.asList("countries", "branches", "calendars", "companies", "currencies", "time_zones");
        List<String> accountKeys = Arrays.asList("country", "branch_code", "calendar_code", "company_code", "currency_code", "time_zone");
        for (int i = 0; i < mapKeys.size(); i++) {
            int finalI = i;
            if (distinctAndSortedValuesOfFields.get(mapKeys.get(i)).stream()
                    .noneMatch(field -> account.get(accountKeys.get(finalI)).equals(field))) {
                distinctAndSortedValuesOfFields.get(mapKeys.get(i)).add(account.get(accountKeys.get(i)));
            }
        }
    }

    private Account fillAccount(Account account, Map<String, String> map, String method) {
        if (method.equals("POST")) {
            account.setCode(map.get("code").toUpperCase());
        } else {
            account.setCode(map.get("code"));
            account.setUuid(UUID.fromString(map.get("uuid")));
        }

        AddressModel_ addressModel = new AddressModel_();
        ReferenceModel country = new ReferenceModel();
        country.setCode(map.get("country"));
        addressModel.setCountry(country);
        addressModel.setCity(map.get("city"));
        account.setAddress(addressModel);

        ReferenceModel currency = new ReferenceModel();
        currency.setCode(map.get("currency_code"));
        account.setCurrency(currency);

        ReferenceModel company = new ReferenceModel();
        company.setCode(map.get("company_code"));
        account.setCompany(company);

        ReferenceModel branch = new ReferenceModel();
        branch.setCode(map.get("branch_code"));
        account.setBranch(branch);

        AccountIdModel accountIdModel = new AccountIdModel();
        accountIdModel.setValue(map.get("value"));
        accountIdModel.setBanStructure(AccountIdModel.BanStructureEnum.fromValue(map.get("ban_structure")));
        account.setBankAccountID(accountIdModel);

        ReferenceModel calendar = new ReferenceModel();
        calendar.setCode(map.get("calendar_code"));
        account.setCalendar(calendar);

        account.setTimeZone(map.get("time_zone"));
        return account;
    }

    private AccountCRUD fillAccountCRUD(AccountCRUD account, Map<String, String> map, String method) {
        if (method.equals("POST")) {
            account.setCode(map.get("code").toUpperCase());
        } else {
            account.setCode(map.get("code"));
            account.setUuid(UUID.fromString(map.get("uuid")));
        }

        AddressModel_ addressModel = new AddressModel_();
        ReferenceModel country = new ReferenceModel();
        country.setCode(map.get("country"));
        addressModel.setCountry(country);
        addressModel.setCity(map.get("city"));
        account.setAddress(addressModel);

        ReferenceModel currency = new ReferenceModel();
        currency.setCode(map.get("currency_code"));
        account.setCurrency(currency);

        ReferenceModel company = new ReferenceModel();
        company.setCode(map.get("company_code"));
        account.setCompany(company);

        ReferenceModel branch = new ReferenceModel();
        branch.setCode(map.get("branch_code"));
        account.setBranch(branch);

        AccountIdModel accountIdModel = new AccountIdModel();
        accountIdModel.setValue(map.get("value"));
        accountIdModel.setBanStructure(AccountIdModel.BanStructureEnum.fromValue(map.get("ban_structure")));
        account.setBankAccountID(accountIdModel);

        ReferenceModel calendar = new ReferenceModel();
        calendar.setCode(map.get("calendar_code"));
        account.setCalendar(calendar);

        account.setTimeZone(map.get("time_zone"));
        return account;
    }
}