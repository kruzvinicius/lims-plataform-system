package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest (
    @NotBlank(message = "corporate reason cannot be blank")
    String corporateReason,

    @NotBlank(message = "email cannot be blank")
    @Email(message = "invalid email")
    String email,

    @NotBlank(message = "tax ID / CNPJ cannot be blank")
    String taxId,

    @NotBlank(message = "phone cannot be blank")
    String phone
) {}
