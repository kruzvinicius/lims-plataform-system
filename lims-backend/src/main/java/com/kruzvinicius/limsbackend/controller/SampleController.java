package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/samples")
public class SampleController {
    @Autowired
    private SampleRepository repository;

    @GetMapping
    public Iterable<Sample> getAllSamples() {
        return repository.findAll();
    }
    @PostMapping
    public Sample createSample(@RequestBody Sample sample) {
        System.out.println("Amostra Recebida:" + sample);
        return repository.save(sample);
    }
}
