package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.model.Customer;
import com.kruzvinicius.limsbackend.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }
}
