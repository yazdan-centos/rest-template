package org.mapnaom.resttemplate.controller;

import org.mapnaom.resttemplate.entity.Employee;
import org.mapnaom.resttemplate.service.EmployeeService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Employee> findAll(Pageable pageable, Specification<Employee> specification) {
        return service.findAll(pageable, specification);
    }

    @GetMapping("/all")
    public List<Employee> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> findById(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee entity) {
        if (service.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = {"/import", "/importByExcel"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importByExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("imported", service.importByExcel(file)));
    }
}
