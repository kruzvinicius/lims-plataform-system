package com.kruzvinicius.limsbackend;

import com.kruzvinicius.limsbackend.model.Customer;
import com.kruzvinicius.limsbackend.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LimsBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LimsBackendApplication.class, args);
    }

//    @Bean
//    CommandLineRunner testDatabase(CustomerRepository customerRepository) {
//        return args -> {
//            System.out.println("--- Iniciando Banco de Dados ---");
//
//            Customer c = new Customer();
//            c.setCorporateReason("Laboratorio Teste Vinicius");
//            c.setEmail("teste@teste.com");
//            c.setPhone("123456789");
//            c.setTaxId("1");
//
//            customerRepository.save(c);
//
//            System.out.println("Cliente salvo com sucesso!");
//        };
//    }
}