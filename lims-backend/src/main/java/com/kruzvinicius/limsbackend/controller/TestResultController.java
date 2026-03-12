package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@Slf4j

public class TestResultController {

    private final TestResultRepository repository;

    @GetMapping
    public List<TestResult> getAllResults() {
        return repository.findAll();
    }

    @PostMapping
    public TestResult createResult(@RequestBody TestResult result) {
        log.info("Recording new test result: {}", result.getParameterName());
        return repository.save(result);
    }
}
