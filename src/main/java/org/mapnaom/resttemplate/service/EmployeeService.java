package org.mapnaom.resttemplate.service;

import org.mapnaom.resttemplate.entity.Employee;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    Employee save(Employee entity);
    Optional<Employee> findById(Long id);
    Page<Employee> findAll(Pageable pageable, Specification<Employee> specification);
    List<Employee> findAll();
    void deleteById(Long id);
    int importByExcel(MultipartFile file);
}
