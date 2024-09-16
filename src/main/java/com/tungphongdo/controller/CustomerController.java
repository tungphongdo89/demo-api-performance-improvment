package com.tungphongdo.controller;


import com.tungphongdo.entity.Customer;
import com.tungphongdo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/large-payload")
    public List<String> getLargePayload() {
        List<String> largePayload = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            largePayload.add("This is line number " + i);
        }
        return largePayload;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.storeCustomerInRedis(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getCustomerFromRedisOrMySQL(id));
    }
}
