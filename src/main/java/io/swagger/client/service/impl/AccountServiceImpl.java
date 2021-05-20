package io.swagger.client.service.impl;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.AccountsApi;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.model.*;
import io.swagger.client.service.AccountService;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {

    public static final String ACCESS_TOKEN_URL = "https://demo.kyriba.com/gateway/oauth/token";
    public static final String CLIENT_ID = "APISANDBOX01@DEV06";
    public static final String CLIENT_SECRET = "PZQSKTpZ9ufHez4y";

    private AccountsApi accountsApi;
    private static String accessToken;
    private List<Account> accounts = new ArrayList<>();

    static {

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
            accessToken = token.getAccessToken();
            //System.out.println(token.getBody()+ "\n");

        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    private static String base64EncodedBasicAuthentication() {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public AccountServiceImpl(AccountsApi accountsApi) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        io.swagger.client.auth.OAuth OAuth2ClientCredentials = (io.swagger.client.auth.OAuth) defaultClient.getAuthentication("OAuth2ClientCredentials");
        OAuth2ClientCredentials.setAccessToken(accessToken);
        this.accountsApi = accountsApi;
    }

    @Override
    public PageOfAccountSearchModel getAllAccounts(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort) {
        PageOfAccountSearchModel result = null;
        try {
            result = accountsApi.readAccountsUsingGET1(activeStatus, filter, pageLimit, pageOffset, sort);
            //System.out.println(result);
        } catch (ApiException e) {
            throw new BadRequestException(e.getResponseBody());
        }
        return result;
    }

    @Override
    public AccountDetailsDto getAccountByCode(String code) {
        AccountDetailsDto result = null;
        try {
            result = accountsApi.readAccountUsingGET1(code);
            //System.out.println(result);
        } catch (ApiException e) {
            throw new BadRequestException(e.getResponseBody());
        }
        return result;
    }

    @Override
    public AccountDetailsDto getAccountByUuid(String uuid) {
        AccountDetailsDto result = null;
        try {
            result = accountsApi.readAccountUsingGET3(UUID.fromString(uuid));
            //System.out.println(result);
        } catch (ApiException e) {
            throw new BadRequestException(e.getMessage());
        }
        return result;
    }

    @Override
    public ResponseIdModel createAccount(@NotNull Map<String, String> account) {
        Account accountDto = new Account();
        accountDto.setCode(account.get("code"));

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
        ResponseIdModel responseIdModel = null;
        try {
            responseIdModel = accountsApi.createUsingPOST1(accountDto);
            //System.out.println(responseIdModel);
        } catch (ApiException e) {
            throw new BadRequestException(e.getResponseBody());
        }
        if (responseIdModel != null) {
            accounts.add(accountDto);
            System.out.println(accounts.size());
        }
        return responseIdModel;
    }

    @Override
    public ResponseIdModel updateAccount(Map<String, String> account) {
        Optional<Account> createdAccount = accounts.stream()
                .filter(account1 -> account.get("code").equals(account1.getCode()))
                .findFirst();

        //accountDetailsDto = accountsApi.readAccountUsingGET1(account.get("code"));
        Account accountDto = new Account();

        if (createdAccount.isPresent()) {
            accountDto.setUuid(createdAccount.get().getUuid());
        } else {
            throw new BadRequestException("There is no account with code " + account.get("code") + " which can be update");
        }

        accountDto.setCode(account.get("code"));

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
        ResponseIdModel responseIdModel = null;
        try {
            responseIdModel = accountsApi.updateUsingPUT1(accountDto, account.get("code"));
            //System.out.println(responseIdModel);
        } catch (ApiException e) {
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
    public ResponseIdModel deleteAccount(String code) {
        Optional<Account> createdAccount = accounts.stream()
                .filter(account1 -> code.equals(account1.getCode()))
                .findFirst();
        if (createdAccount.isEmpty()) {
            throw new BadRequestException("There is no account with code " + code + " which can be delete");
        }

        AccountsApi accountsApi = new AccountsApi();
        ResponseIdModel responseIdModel = null;
        try {
            responseIdModel = accountsApi.deleteByCodeUsingDELETE1(code);
            //System.out.println(responseIdModel);
        } catch (ApiException e) {
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
}