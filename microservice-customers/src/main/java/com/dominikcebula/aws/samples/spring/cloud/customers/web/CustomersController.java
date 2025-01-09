package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CustomersController {
    @GetMapping("/customers")
    public String getCustomers() {
        return "OK";
    }
}
