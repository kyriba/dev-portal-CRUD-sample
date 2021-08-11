
package io.swagger.client.service;

import io.swagger.client.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApiService {

    String getAll(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort);

    String getByCode(String code);

    String  getByUuid(String uuid);

    ResponseIdModel create(Map<String, String> account);

    ResponseIdModel update(Map<String, String> account);

    ResponseIdModel delete(String code);

    String getRequestBody();

    List<String> getCreatedCodes();

    List<String> getAllCodes();

    Map<String, Set<String>> getSortedDistinctValuesOfFields();

    Map<String, List<String>> getAvailableValues();

    Map<String, Set<String>> getByCodeToUpdate(String code);
}