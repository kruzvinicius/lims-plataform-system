package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.CustomerRequest;
import com.kruzvinicius.limsbackend.dto.CustomerResponse;
import com.kruzvinicius.limsbackend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Valid CustomerRequest request) {
        var response = customerService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}