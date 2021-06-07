package io.swagger.client.service.impl;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.AccountsApi;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.exception.InvalidTokenException;
import io.swagger.client.model.*;
import io.swagger.client.service.AccountService;
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
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Value("${access.token.url}")
    public String ACCESS_TOKEN_URL;
    @Value("${client.id}")
    public String CLIENT_ID;
    @Value("${client.secret}")
    public String CLIENT_SECRET;

    private AccountsApi accountsApi;
    private List<Account> accounts = new ArrayList<>();

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
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            io.swagger.client.auth.OAuth OAuth2ClientCredentials = (io.swagger.client.auth.OAuth) defaultClient.getAuthentication("OAuth2ClientCredentials");
            OAuth2ClientCredentials.setAccessToken(accessToken);

        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }

    @Override
    public List<Account> getCreatedAccounts() {
        return accounts;
    }

    private String base64EncodedBasicAuthentication() {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public AccountServiceImpl(AccountsApi accountsApi) {
        this.accountsApi = accountsApi;
    }

    @PostConstruct
    private void authenticate() {
        refreshToken();
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public PageOfAccountSearchModel getAllAccounts(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort) {
        PageOfAccountSearchModel result;
        try {
            result = accountsApi.readAccountsUsingGET1(activeStatus, filter, pageLimit, pageOffset, sort);
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
    public AccountDetailsDto getAccountByCode(String code) {
        AccountDetailsDto result;
        try {
            result = accountsApi.readAccountUsingGET1(code);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no account with code " + code);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public AccountDetailsDto getAccountByUuid(String uuid) {
        AccountDetailsDto result;
        try {
            result = accountsApi.readAccountUsingGET3(UUID.fromString(uuid));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException("There is no account with uuid " + uuid);
        }
        return result;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel createAccount(Map<String, String> account) {
        Account accountDto = new Account();
        accountDto.setCode(account.get("code").toUpperCase());

        AddressModel_ addressModel = new AddressModel_();
        ReferenceModel country = new ReferenceModel();
        country.setCode(account.get("country"));
        addressModel.setCountry(country);
        addressModel.setCity(account.get("city"));
        accountDto.setAddress(addressModel);

        ReferenceModel currency = new ReferenceModel();
        currency.setCode(account.get("currency_code"));
        accountDto.setCurrency(currency);

        ReferenceModel company = new ReferenceModel();
        company.setCode(account.get("company_code"));
        accountDto.setCompany(company);

        ReferenceModel branch = new ReferenceModel();
        branch.setCode(account.get("branch_code"));
        accountDto.setBranch(branch);

        AccountIdModel accountIdModel = new AccountIdModel();
        accountIdModel.setValue(account.get("value"));
        accountIdModel.setBanStructure(AccountIdModel.BanStructureEnum.fromValue(account.get("ban_structure")));
        accountDto.setBankAccountID(accountIdModel);

        ReferenceModel calendar = new ReferenceModel();
        calendar.setCode(account.get("calendar_code"));
        accountDto.setCalendar(calendar);

        accountDto.setTimeZone(account.get("time_zone"));

        AccountsApi accountsApi = new AccountsApi();
        ResponseIdModel responseIdModel;
        try {
            responseIdModel = accountsApi.createUsingPOST1(accountDto);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            accountDto.setUuid(responseIdModel.getUuid());
            accounts.add(accountDto);
            System.out.println(accounts.size());
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel updateAccount(Map<String, String> account) {

        Account accountDto = new Account();

        accountDto.setCode(account.get("code"));

        accountDto.setUuid(UUID.fromString(account.get("uuid")));

        AddressModel_ addressModel = new AddressModel_();
        ReferenceModel country = new ReferenceModel();
        country.setCode(account.get("country"));
        addressModel.setCountry(country);
        addressModel.setCity(account.get("city"));
        accountDto.setAddress(addressModel);

        ReferenceModel currency = new ReferenceModel();
        currency.setCode(account.get("currency_code"));
        accountDto.setCurrency(currency);

        ReferenceModel company = new ReferenceModel();
        company.setCode(account.get("company_code"));
        accountDto.setCompany(company);

        ReferenceModel branch = new ReferenceModel();
        branch.setCode(account.get("branch_code"));
        accountDto.setBranch(branch);

        AccountIdModel accountIdModel = new AccountIdModel();
        accountIdModel.setValue(account.get("value"));
        accountIdModel.setBanStructure(AccountIdModel.BanStructureEnum.fromValue(account.get("ban_structure")));
        accountDto.setBankAccountID(accountIdModel);

        ReferenceModel calendar = new ReferenceModel();
        calendar.setCode(account.get("calendar_code"));
        accountDto.setCalendar(calendar);

        accountDto.setTimeZone(account.get("time_zone"));

        AccountsApi accountsApi = new AccountsApi();
        ResponseIdModel responseIdModel;
        try {
            responseIdModel = accountsApi.updateUsingPUT1(accountDto, account.get("code"));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            removeAccountByCode(account.get("code"));
            accounts.add(accountDto);
            System.out.println(accounts.size());
        }
        return responseIdModel;
    }

    @Override
    @Retryable(value = InvalidTokenException.class, maxAttempts = 2)
    public ResponseIdModel deleteAccount(String code) {
        Optional<Account> createdAccount = accounts.stream()
                .filter(account1 -> code.equals(account1.getCode()))
                .findFirst();
        if (!createdAccount.isPresent()) {
            throw new BadRequestException("There is no account with code " + code + " which can be delete");
        }

        AccountsApi accountsApi = new AccountsApi();
        ResponseIdModel responseIdModel;
        try {
            responseIdModel = accountsApi.deleteByCodeUsingDELETE1(code);
        } catch (ApiException e) {
            if (e.getResponseBody().contains("invalid_token")) {
                refreshToken();
                throw new InvalidTokenException(e.getResponseBody());
            } else
                throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            removeAccountByCode(code);
            System.out.println(accounts.size());
        }
        return responseIdModel;
    }

    private void removeAccountByCode(String code) {
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
        PageOfAccountSearchModel result = getAllAccounts(null, null, null, null, null);
        for (int i = 0; i < result.getResults().size(); i++) {
            codes.add(result.getResults().get(i).getCode());
        }
        return codes;
    }

    @Override
    public Map<String, List<String>> getAndSortDistinctValuesOfAccountsFields() {
        Map<String, List<String>> fields = new HashMap<>();
        List<String> codes = getAllCodes();
        List<AccountDetailsDto> accountDetailsDtoList = new ArrayList<>();
        for (String code : codes) {
            accountDetailsDtoList.add(getAccountByCode(code));
        }
        List<String> country_codes = accountDetailsDtoList.stream()
                .map(accountDetailsDto -> accountDetailsDto.getAddress().getCountry().getCode())
                .distinct().sorted().collect(Collectors.toList());
        fields.put("countries", country_codes);

        List<String> ban_structures = Arrays.stream(AccountIdModel.BanStructureEnum.values())
                .map(AccountIdModel.BanStructureEnum::getValue)
                .collect(Collectors.toList());
        fields.put("ban_structures", ban_structures);

        List<String> branch_codes = accountDetailsDtoList.stream()
                .map(accountDetailsDto -> accountDetailsDto.getBranch().getCode())
                .distinct().sorted().collect(Collectors.toList());
        fields.put("branches", branch_codes);

        List<String> calendar_codes = accountDetailsDtoList.stream()
                .map(accountDetailsDto -> accountDetailsDto.getCalendar().getCode())
                .distinct().sorted().collect(Collectors.toList());
        fields.put("calendars", calendar_codes);

        List<String> company_codes = accountDetailsDtoList.stream()
                .map(accountDetailsDto -> accountDetailsDto.getCompany().getCode())
                .distinct().sorted().collect(Collectors.toList());
        fields.put("companies", company_codes);

        List<String> currency_codes = accountDetailsDtoList.stream()
                .map(accountDetailsDto -> accountDetailsDto.getCurrency().getCode())
                .distinct().sorted().collect(Collectors.toList());
        fields.put("currencies", currency_codes);

        List<String> time_zones = accountDetailsDtoList.stream()
                .map(AccountDetailsDto::getTimeZone)
                .distinct().sorted().collect(Collectors.toList());
        fields.put("time_zones", time_zones);

        return fields;
    }
}