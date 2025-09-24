package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.ankurshala.backend.entity.AccountType;

public class TeacherBankDetailsDto {
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    private String bankName;
    
    @Size(max = 500)
    private String branchAddress;
    
    @NotBlank
    @Size(max = 50)
    private String accountNumber;
    
    @NotBlank
    @Size(max = 20)
    private String ifscCode;
    
    @NotBlank
    @Size(max = 100)
    private String accountHolderName;
    
    private AccountType accountType;

    // Constructors
    public TeacherBankDetailsDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
