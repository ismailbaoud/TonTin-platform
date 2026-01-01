package com.tontin.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class test {
    
    @GetMapping
    public ResponseEntity<String> testMethod() {
        return ResponseEntity.ok().body("it's working perfectly in docker");
    }
}
