package io.swagger.client.controller;

import io.swagger.client.JSON;
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
    private JSON json;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
        json = new JSON();
    }

    @GetMapping
    public String getMenu(Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        return "html/menu";
    }

    @GetMapping("/getAll")
    public String getAll(Model model) {
        model.addAttribute("port", PORT);
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("list_accounts", apiService.getAll(null, null, null, null, null));
        return "html/accounts-list";
    }

    @PostMapping("/getByCode")
    public String getByCode(@RequestParam String get_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        if (get_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", apiService.getByCode(get_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no result with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        return "html/account-code-renderjson";
    }

    @PostMapping("/getByUuid")
    public String getByUuid(@RequestParam String get_uuid, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        if (get_uuid.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", apiService.getByUuid(get_uuid));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no result with uuid")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        } catch (IllegalArgumentException e2) {
            model.addAttribute("error_message", json.serialize(e2.getMessage()));
            return "exception/bad-request-exception";
        }
        return "html/account-uuid-renderjson";
    }

    @PostMapping("/create")
    public String createAccount(Model model) {
        model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
        return "html/create-account";
    }

    @PostMapping("/created")
    public String createdAccount(@RequestParam Map<String, String> requestParams, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("responseModel", apiService.create(requestParams));
        } catch (BadRequestException e) {
            model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
            model.addAttribute("error_message", e.getMessage());
            model.addAttribute("inputFields", requestParams.keySet());
            model.addAttribute("inputs", requestParams.values());
            return "html/create-account";
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("request_body", apiService.getRequestBody());
        return "html/new-account";
    }

    @PostMapping("/update")
    public String updateAccount(@RequestParam String update_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        if (update_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            if (apiService.getCreatedCodes().contains(update_code)) {
                model.addAttribute("account", apiService.getByCode(update_code));
            } else
                throw new BadRequestException("There is no result with code " + update_code + " which can be update");
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no result with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
        return "html/update-account";
    }

    @PostMapping("/updated")
    public String updatedAccount(@RequestParam Map<String, String> account, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("account", apiService.update(account));
        } catch (BadRequestException e) {
            model.addAttribute("fields", apiService.getSortedDistinctValuesOfFields());
            model.addAttribute("error_message", e.getMessage());
            model.addAttribute("account", apiService.getByCode(account.get("code").toUpperCase()));
            return "html/update-account";
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("request_body", apiService.getRequestBody());
        return "html/updated-account";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String delete_code, Model model) {
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        model.addAttribute("base_url", BASE_URL);
        if (delete_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", apiService.delete(delete_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no result with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("codes_list", apiService.getAllCodes());
        model.addAttribute("created_codes", apiService.getCreatedCodes());
        return "html/deleted-account";
    }
}
