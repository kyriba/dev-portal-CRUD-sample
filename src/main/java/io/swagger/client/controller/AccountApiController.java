package io.swagger.client.controller;

import io.swagger.client.exception.BadRequestException;
import io.swagger.client.model.*;
import io.swagger.client.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/accounts")
public class AccountApiController {

    @Value("${base.url}")
    private String BASE_URL;
    private AccountService accountService;

    public AccountApiController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String getMenu(Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        return "html/menu";
    }

    @GetMapping("/getAll")
    @ResponseBody
    public PageOfAccountSearchModel getAll() {
        return accountService.getAllAccounts(null, null, null, null, null);
    }

    @GetMapping("/getAllAccounts")
    public String getAllAccounts(Model model) {
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        model.addAttribute("base_url", BASE_URL);
        return "html/accounts-list";
    }

    @GetMapping("/getByCode")
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
                model.addAttribute("error_message", e.getMessage());
                return "exception/bad-request-exception";
            }
        }
        System.out.println(get_code);
        return "html/account-code-renderjson";
    }

    @GetMapping("/getByUuid")
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
                model.addAttribute("error_message", e.getMessage());
                return "exception/bad-request-exception";
            }
        } catch (IllegalArgumentException e2) {
            model.addAttribute("error_message", e2.getMessage());
            return "exception/bad-request-exception";
        }
        System.out.println(get_uuid);
        return "html/account-uuid-renderjson";
    }

    @GetMapping("/create")
    public String createAccount(Model model) {
        model.addAttribute("fields", accountService.getAndSortDistinctValuesOfAccountsFields());
        return "html/create-account";
    }

    @GetMapping("/created")
    public String createdAccount(@RequestParam Map<String, String> account, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("account", accountService.createAccount(account));
        } catch (BadRequestException e) {
            model.addAttribute("codes_list", accountService.getAllCodes());
            model.addAttribute("created_accounts", accountService.getCreatedAccounts());
            model.addAttribute("error_message", e.getMessage());
            return "exception/bad-request-exception";
        }
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        return "html/new-account";
    }

    @GetMapping("/update")
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
                model.addAttribute("error_message", e.getMessage());
                return "exception/bad-request-exception";
            }
        }
        model.addAttribute("fields", accountService.getAndSortDistinctValuesOfAccountsFields());
        return "html/update-account";
    }

    @GetMapping("/updated")
    public String updatedAccount(@RequestParam Map<String, String> account, Model model) {
        model.addAttribute("base_url", BASE_URL);
        try {
            model.addAttribute("account", accountService.updateAccount(account));
        } catch (BadRequestException e) {
            model.addAttribute("codes_list", accountService.getAllCodes());
            model.addAttribute("created_accounts", accountService.getCreatedAccounts());
            model.addAttribute("error_message", e.getMessage());
            return "exception/bad-request-exception";
        }
        model.addAttribute("codes_list", accountService.getAllCodes());
        model.addAttribute("created_accounts", accountService.getCreatedAccounts());
        return "html/updated-account";
    }

    @GetMapping("/delete")
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
                model.addAttribute("error_message", e.getMessage());
                return "exception/bad-request-exception";
            }
        }
        return "html/deleted-account";
    }
}
