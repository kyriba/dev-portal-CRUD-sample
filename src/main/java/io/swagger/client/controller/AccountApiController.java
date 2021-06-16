package io.swagger.client.controller;

import io.swagger.client.JSON;
import io.swagger.client.exception.BadRequestException;
import io.swagger.client.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/CRUD")
public class AccountApiController {

    @Value("${base.url}")
    private String BASE_URL;
    @Value("${server.port}")
    private String PORT;
    private AccountService accountService;
    private JSON json;

    public AccountApiController(AccountService accountService) {
        this.accountService = accountService;
        json = new JSON();
    }

    @GetMapping
    public String getMenu(Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        return "html/menu";
    }

    @GetMapping("/getAll")
    public String getAllAccounts(Model model) {
        model.addAttribute("port", PORT);
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        model.addAttribute("list_accounts", accountService.getAllAccounts(null, null, null, null, null));
        return "html/accounts-list";
    }

    @PostMapping("/getByCode")
    public String getByCode(@RequestParam String get_code, Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        if (get_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", accountService.getAccountByCode(get_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no account with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        return "html/account-code-renderjson";
    }

    @PostMapping("/getByUuid")
    public String getByUuid(@RequestParam String get_uuid, Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        if (get_uuid.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", accountService.getAccountByUuid(get_uuid));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no account with uuid")) {
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
        model.addAttribute("fields", accountService.getSortedDistinctValuesOfAccountsFields());
        return "html/create-account";
    }

    @PostMapping("/created")
    public String createdAccount(@RequestParam Map<String, String> account, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("account", accountService.createAccount(account));
        } catch (BadRequestException e) {
            model.addAttribute("fields", accountService.getSortedDistinctValuesOfAccountsFields());
            model.addAttribute("error_message", e.getMessage());
            model.addAttribute("account", account);
            return "html/create-account";
        }
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        accountService.getCreatedAccounts().stream()
                .filter(account1 -> account1.getCode().equals(account.get("code")))
                .findFirst().ifPresent(value -> model.addAttribute("request_body", value));
        return "html/new-account";
    }

    @PostMapping("/update")
    public String updateAccount(@RequestParam String update_code, Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        if (update_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            if (accountService.getCreatedAccounts().stream()
                    .anyMatch(account -> update_code.equals(account.getCode()))) {
                model.addAttribute("account", accountService.getAccountByCode(update_code));
            } else
                throw new BadRequestException("There is no account with code " + update_code + " which can be update");
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no account with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("fields", accountService.getSortedDistinctValuesOfAccountsFields());
        return "html/update-account";
    }

    @PostMapping("/updated")
    public String updatedAccount(@RequestParam Map<String, String> account, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("account", accountService.updateAccount(account));
        } catch (BadRequestException e) {
            model.addAttribute("error_message", e.getMessage());
            updateAccount(account.get("code"), model);
        }
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        accountService.getCreatedAccounts().stream()
                .filter(account1 -> account1.getCode().equals(account.get("code")))
                .findFirst().ifPresent(value -> model.addAttribute("request_body", value));
        return "html/updated-account";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String delete_code, Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        if (delete_code.equals("")) {
            return "exception/blank-input-exception";
        }
        try {
            model.addAttribute("account", accountService.deleteAccount(delete_code));
        } catch (BadRequestException e) {
            if (e.getMessage().contains("There is no account with code")) {
                model.addAttribute("error_message", json.serialize(e.getMessage()));
                return "exception/bad-request-exception";
            }
        }
        return "html/deleted-account";
    }
}
