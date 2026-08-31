package org.mapnaom.resttemplate.repository;

import org.mapnaom.resttemplate.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByPersonnelCode(Long personnelCode);
}
