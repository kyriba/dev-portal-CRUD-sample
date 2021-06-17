package io.swagger.client.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;

/**
 * AccountCRUD.
 */
@ApiModel(description = "AccountCRUD.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-17T11:52:10.236Z")
public class AccountCRUD {
    @SerializedName("uuid")
    private UUID uuid = null;

    @SerializedName("code")
    private String code = null;

    @SerializedName("currency")
    private ReferenceModel currency = null;

    @SerializedName("company")
    private ReferenceModel company = null;

    @SerializedName("branch")
    private ReferenceModel branch = null;

    @SerializedName("bankAccountID")
    private AccountIdModel bankAccountID = null;

    @SerializedName("address")
    private AddressModel_ address = null;

    @SerializedName("calendar")
    private ReferenceModel calendar = null;

    @SerializedName("timeZone")
    private String timeZone = null;

    public AccountCRUD uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * UUID of the account.
     * @return uuid
     **/
    @ApiModelProperty(example = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", required = true, value = "UUID of the account.")
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public AccountCRUD code(String code) {
        this.code = code;
        return this;
    }

    /**
     * Code that represents the account.
     * @return code
     **/
    @ApiModelProperty(example = "ACCOUNT001", required = true, value = "Code that represents the account.")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AccountCRUD address(AddressModel_ address) {
        this.address = address;
        return this;
    }

    /**
     * Account&#39;s address.
     * @return address
     **/
    @ApiModelProperty(required = true, value = "Account's address.")
    public AddressModel_ getAddress() {
        return address;
    }

    public void setAddress(AddressModel_ address) {
        this.address = address;
    }

    public AccountCRUD bankAccountID(AccountIdModel bankAccountID) {
        this.bankAccountID = bankAccountID;
        return this;
    }

    /**
     * Account code according to the international banking standards (BAN).
     * @return bankAccountID
     **/
    @ApiModelProperty(required = true, value = "Account code according to the international banking standards (BAN).")
    public AccountIdModel getBankAccountID() {
        return bankAccountID;
    }

    public void setBankAccountID(AccountIdModel bankAccountID) {
        this.bankAccountID = bankAccountID;
    }

    public AccountCRUD branch(ReferenceModel branch) {
        this.branch = branch;
        return this;
    }

    /**
     * Bank branch that holds the account.
     * @return branch
     **/
    @ApiModelProperty(required = true, value = "Bank branch that holds the account.")
    public ReferenceModel getBranch() {
        return branch;
    }

    public void setBranch(ReferenceModel branch) {
        this.branch = branch;
    }

    public AccountCRUD calendar(ReferenceModel calendar) {
        this.calendar = calendar;
        return this;
    }

    /**
     * Account&#39;s calendar.
     * @return calendar
     **/
    @ApiModelProperty(required = true, value = "Account's calendar.")
    public ReferenceModel getCalendar() {
        return calendar;
    }

    public void setCalendar(ReferenceModel calendar) {
        this.calendar = calendar;
    }

    public AccountCRUD company(ReferenceModel company) {
        this.company = company;
        return this;
    }

    /**
     * Company of the account.
     * @return company
     **/
    @ApiModelProperty(required = true, value = "Company of the account.")
    public ReferenceModel getCompany() {
        return company;
    }

    public void setCompany(ReferenceModel company) {
        this.company = company;
    }

    public AccountCRUD currency(ReferenceModel currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Currency of the account.
     * @return currency
     **/
    @ApiModelProperty(required = true, value = "Currency of the account.")
    public ReferenceModel getCurrency() {
        return currency;
    }

    public void setCurrency(ReferenceModel currency) {
        this.currency = currency;
    }

    public AccountCRUD timeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * Account&#39;s time zone.
     * @return timeZone
     **/
    @ApiModelProperty(example = "GMT", required = true, value = "Account's time zone.")
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountCRUD account = (AccountCRUD) o;
        return Objects.equals(this.uuid, account.uuid) &&
                Objects.equals(this.code, account.code) &&
                Objects.equals(this.currency, account.currency) &&
                Objects.equals(this.company, account.company) &&
                Objects.equals(this.branch, account.branch) &&
                Objects.equals(this.bankAccountID, account.bankAccountID) &&
                Objects.equals(this.address, account.address) &&
                Objects.equals(this.calendar, account.calendar) &&
                Objects.equals(this.timeZone, account.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, code, currency, company, branch, bankAccountID, address, calendar, timeZone);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountCRUD {\n");

        sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    bankAccountID: ").append(toIndentedString(bankAccountID)).append("\n");
        sb.append("    branch: ").append(toIndentedString(branch)).append("\n");
        sb.append("    calendar: ").append(toIndentedString(calendar)).append("\n");
        sb.append("    company: ").append(toIndentedString(company)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    timeZone: ").append(toIndentedString(timeZone)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

