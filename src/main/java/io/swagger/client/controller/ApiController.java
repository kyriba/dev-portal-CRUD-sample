package io.swagger.client.controller;

import io.swagger.client.JSON;
import io.swagger.client.config.InitialApiBean;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/CRUD")
public class ApiController {

    @Value("${base.url}")
    private String BASE_URL;
    @Value("${server.port}")
    private String PORT;
    private final ApiService apiService;
    private final InitialApiBean apiBean;
    private final JSON json;

    public ApiController(ApiService apiService, InitialApiBean apiBean) {
        this.apiService = apiService;
        this.apiBean = apiBean;
        json = new JSON();
    }

    @GetMapping
    public String getMenu(Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        return "html/menu";
    }

    @GetMapping("/getAll")
    public String getAll(Model model) {
        model.addAttribute("port", PORT);
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        model.addAttribute("list_items", apiService.getAll(null, null, null, null, null));
        return "html/items-list";
    }

    @PostMapping("/getByCode")
    public String getByCode(@RequestParam String get_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        if (get_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("item", apiService.getByCode(get_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no item with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        return "html/item-code-renderjson";
    }

    @PostMapping("/getByUuid")
    public String getByUuid(@RequestParam String get_uuid, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        if (get_uuid.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("item", apiService.getByUuid(get_uuid));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no item with uuid")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        } catch (IllegalArgumentException e2) {
            model.addAttribute("error_message", json.serialize(e2.getMessage()));
            return "exception/bad-request-exception";
        }
        return "html/item-uuid-renderjson";
    }

    @PostMapping("/create")
    public String createItem(Model model) {
        model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
        model.addAttribute("available_values", apiService.getAvailableValues());
        return "html/create-item";
    }

    @PostMapping("/created")
    public String createdItem(@RequestParam Map<String, String> requestParams, Model model) {
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        try {
            model.addAttribute("responseModel", apiService.create(requestParams));
        } catch (BadRequestException e) {
            model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
            model.addAttribute("available_values", apiService.getAvailableValues());
            model.addAttribute("error_message", e.getMessage());
            model.addAttribute("inputFields", requestParams.keySet());
            model.addAttribute("inputs", requestParams.values());
            return "html/create-item";
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("request_body", apiService.getRequestBody());
        return "html/new-item";
    }

    @PostMapping("/update")
    public String updateItem(@RequestParam String update_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        if (update_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            if (apiService.getCreatedCodes().contains(update_code)) {
                model.addAttribute("initial_data", apiService.getByCodeToUpdate(update_code));
            } else
                throw new BadRequestException("There is no item with code " + update_code + " which can be update");
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no item with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
        model.addAttribute("available_values", apiService.getAvailableValues());
        return "html/update-item";
    }

    @PostMapping("/updated")
    public String updatedItem(@RequestParam Map<String, String> item, Model model) {
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        try {
            model.addAttribute("item", apiService.update(item));
        } catch (BadRequestException e) {
            model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
            model.addAttribute("available_values", apiService.getAvailableValues());
            model.addAttribute("error_message", e.getMessage());
            model.addAttribute("initial_data", apiService.getByCodeToUpdate(item.get("code")));
            return "html/update-item";
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("request_body", apiService.getRequestBody());
        return "html/updated-item";
    }

    @PostMapping("/delete")
    public String deleteItem(@RequestParam String delete_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("api_url", apiBean.getApiName());
        if (delete_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("item", apiService.delete(delete_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no item with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        return "html/deleted-item";
    }
}
