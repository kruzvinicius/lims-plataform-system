package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.CustomerRequest;
import com.kruzvinicius.limsbackend.dto.CustomerResponse;
import com.kruzvinicius.limsbackend.model.Customer;
import com.kruzvinicius.limsbackend.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    /**
     * search all customers and map to DTO
     */
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Receives a DTO and creates a new Customer entity.
     */
    @Transactional
    public CustomerResponse save(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setCorporateReason(request.corporateReason());
        customer.setEmail(request.email());
        customer.setTaxId(request.taxId());
        customer.setPhone(request.phone());

        Customer savedCustomer = repository.save(customer);

        return mapToResponse(savedCustomer);
    }

    /**
     * auxiliary method to map Customer entity to DTO
     */
    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCorporateReason(),
                customer.getEmail(),
                customer.getTaxId(),
                customer.getPhone()
        );
    }
}