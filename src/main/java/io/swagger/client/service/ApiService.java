
package io.swagger.client.service;

import io.swagger.client.ResponseIdModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApiService {

    String getAll(String activeStatus, String filter, Integer pageLimit, Integer pageOffset, List<String> sort);

    String getByCode(String code);

    String getByUuid(String uuid);

    String getByRef(String ref);

    ResponseIdModel create(Map<String, String> item);

    ResponseIdModel update(Map<String, String> item);

    ResponseIdModel deleteByCode(String code);

    ResponseIdModel deleteByUuid(String uuid);

    String getRequestBody();

    List<String> getCreatedCodes();

    List<String> getAllCodes();

    Map<String, Set<String>> getSortedDistinctValuesOfFields();

    Map<String, List<String>> getAvailableValues();

    List<String> getApiMethods();

    List<String> getApiFields();

    Map<String, Set<String>> getByCodeToUpdate(String code);

    Map<String, Set<String>> getByUuidToUpdate(String uuid);
}