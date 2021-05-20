
package io.swagger.client.service;

import io.swagger.client.model.AccountDetailsDto;
import io.swagger.client.model.PageOfAccountSearchModel;
import io.swagger.client.model.ResponseIdModel;

import java.util.List;
import java.util.Map;

public interface AccountService {
    PageOfAccountSearchModel getAllAccounts(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort);

    AccountDetailsDto getAccountByCode(String code);

    AccountDetailsDto getAccountByUuid(String uuid);

    ResponseIdModel createAccount(Map<String, String> account);

    ResponseIdModel updateAccount(Map<String, String> account);

    ResponseIdModel deleteAccount(String code);
}