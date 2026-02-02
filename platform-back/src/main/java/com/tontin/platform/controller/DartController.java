package com.tontin.platform.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tontin.platform.dto.Dart.request.DartRequest;
import com.tontin.platform.dto.Dart.response.DartResponse;
import com.tontin.platform.service.impl.DartServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dart")
public class DartController {
    
    private final DartServiceImpl dartService;
    
    @GetMapping("/{id}")
    public ResponseEntity<DartResponse> getDart(@PathVariable("id") UUID id) {
        return ResponseEntity.ok().body(dartService.getDartDetails(id));
    }
    
    @PostMapping
    public ResponseEntity<DartResponse> createDart(@Valid @RequestBody DartRequest request) {
        return ResponseEntity.ok().body(dartService.createDart(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DartResponse> updateDart(@RequestBody DartRequest request,@PathVariable UUID id ) {
        return ResponseEntity.ok().body(dartService.updateDart(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DartResponse> deleteDart(@PathVariable UUID id) {
        return ResponseEntity.ok().body(dartService.deleteDart(id));
    }
}
