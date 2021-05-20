package io.swagger.client.controller;

import io.swagger.client.model.*;
import io.swagger.client.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/accounts")
public class AccountApiController {
    private AccountService accountService;

    public AccountApiController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/getAll")
    @ResponseBody
    public PageOfAccountSearchModel getAll(){
        return accountService.getAllAccounts(null, null, null, null, null);
    }

    @GetMapping("/getAccountByCode")
    @ResponseBody
    public AccountDetailsDto getAccountByCode(@RequestParam String code){
        return accountService.getAccountByCode(code);
    }

    @GetMapping("/getAccountByUuid")
    @ResponseBody
    public AccountDetailsDto getAccountByUuid(@RequestParam String uuid){
        return accountService.getAccountByUuid(uuid);
    }

    @GetMapping("/new")
    @ResponseBody
    public ResponseIdModel createdAccount(@RequestParam Map<String, String> account) {
        System.out.println(account.entrySet());
        return accountService.createAccount(account);
    }

    @GetMapping("/changed")
    @ResponseBody
    public ResponseIdModel changedAccount(@RequestParam Map<String, String> account) {
        System.out.println(account.entrySet());
        return accountService.updateAccount(account);
    }

    @GetMapping("/deleted")
    @ResponseBody
    public ResponseIdModel deletedAccount(@RequestParam String code) {
        return accountService.deleteAccount(code);
    }


    @GetMapping
    public String getMenu(){
        return "menu";
    }

    @GetMapping("/getAllAccounts")
    public String getAllAccounts(){
        return "accounts-list";
    }

    @GetMapping("/create")
    public String createAccount(){
        return "create-account";
    }

    @GetMapping("/update")
    public String updateAccount(@RequestParam String update_code, Model model){
        model.addAttribute("code", update_code);
        model.addAttribute("country", accountService.getAccountByCode(update_code).getAddress().getCountry().getCode());
        model.addAttribute("city", accountService.getAccountByCode(update_code).getAddress().getCity());
        model.addAttribute("value", accountService.getAccountByCode(update_code).getBankAccountID().getValue());
        model.addAttribute("ban_structure", accountService.getAccountByCode(update_code).getBankAccountID().getBanStructure());
        model.addAttribute("branch_code", accountService.getAccountByCode(update_code).getBranch().getCode());
        model.addAttribute("calendar_code", accountService.getAccountByCode(update_code).getCalendar().getCode());
        model.addAttribute("company_code", accountService.getAccountByCode(update_code).getCompany().getCode());
        model.addAttribute("currency_code", accountService.getAccountByCode(update_code).getCurrency().getCode());
        model.addAttribute("time_zone", accountService.getAccountByCode(update_code).getTimeZone());
        return "update-account";
    }


    @GetMapping("/getByCode")
    public String getByCode(@RequestParam String get_code, Model model){
        model.addAttribute("code", get_code);
        System.out.println(get_code);
        return "account-code-renderjson";
    }

    @GetMapping("/getByUuid")
    public String getByUuid(@RequestParam String get_uuid, Model model){
        model.addAttribute("uuid", get_uuid);
        System.out.println(get_uuid);
        return "account-uuid-renderjson";
    }

    @GetMapping("/created")
    public String createdAccount(@RequestParam Map<String, String> account, Model model){
        model.addAttribute("code", account.get("code"));
        model.addAttribute("country", account.get("country"));
        model.addAttribute("city", account.get("city"));
        model.addAttribute("value", account.get("value"));
        model.addAttribute("ban_structure", account.get("ban_structure"));
        model.addAttribute("branch_code", account.get("branch_code"));
        model.addAttribute("calendar_code", account.get("calendar_code"));
        model.addAttribute("company_code", account.get("company_code"));
        model.addAttribute("currency_code", account.get("currency_code"));
        model.addAttribute("time_zone", account.get("time_zone"));
        return "new-account";
    }

    @GetMapping("/updated")
    public String updatedAccount(@RequestParam Map<String, String> account, Model model){
        model.addAttribute("code", account.get("code"));
        model.addAttribute("country", account.get("country"));
        model.addAttribute("city", account.get("city"));
        model.addAttribute("value", account.get("value"));
        model.addAttribute("ban_structure", account.get("ban_structure"));
        model.addAttribute("branch_code", account.get("branch_code"));
        model.addAttribute("calendar_code", account.get("calendar_code"));
        model.addAttribute("company_code", account.get("company_code"));
        model.addAttribute("currency_code", account.get("currency_code"));
        model.addAttribute("time_zone", account.get("time_zone"));
        return "updated-account";
    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam String delete_code, Model model){
        model.addAttribute("code", delete_code);
        return "delete-account";
    }
}
