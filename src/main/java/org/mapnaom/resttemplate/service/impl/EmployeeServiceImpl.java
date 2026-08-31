package org.mapnaom.resttemplate.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.mapnaom.resttemplate.entity.*;
import org.mapnaom.resttemplate.repository.*;
import org.mapnaom.resttemplate.service.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repository;
    private final PostRepository postRepository;
    private final WorkLocationRepository locationRepository;

    public EmployeeServiceImpl(EmployeeRepository repository, PostRepository postRepository, WorkLocationRepository locationRepository) {
        this.repository = repository;
        this.postRepository = postRepository;
        this.locationRepository = locationRepository;
    }

    public Employee save(Employee entity) {
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Employee> findAll(Pageable pageable, Specification<Employee> specification) {
        return specification == null ? repository.findAll(pageable) : repository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return repository.findAll();
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public int importByExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ExcelImportException("Excel file is empty");
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw new ExcelImportException("Excel header is missing");
            int code = ExcelSupport.column(header, "personnelCode", "personnel_code", "کد پرسنلی");
            int first = ExcelSupport.column(header, "firstName", "first_name", "نام");
            int last = ExcelSupport.column(header, "lastName", "last_name", "نام خانوادگی");
            int full = ExcelSupport.column(header, "fullName", "full_name", "نام و نام خانوادگی");
            int gender = ExcelSupport.column(header, "gender", "جنسیت");
            int post = ExcelSupport.column(header, "post", "پست");
            int location = ExcelSupport.column(header, "workLocation", "work_location", "محل خدمت");
            if (code < 0 || first < 0 || last < 0)
                throw new ExcelImportException("Required employee columns are missing");
            int count = 0;
            for (int i = header.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || ExcelSupport.text(row.getCell(code)).isBlank()) continue;
                Long personnelCode = Long.valueOf(ExcelSupport.required(row, code, "personnelCode").replace(",", ""));
                Employee employee = repository.findByPersonnelCode(personnelCode).orElseGet(Employee::new);
                employee.setPersonnelCode(personnelCode);
                employee.setFirstName(ExcelSupport.required(row, first, "firstName"));
                employee.setLastName(ExcelSupport.required(row, last, "lastName"));
                String fullName = full < 0 ? employee.getFirstName() + " " + employee.getLastName() : ExcelSupport.text(row.getCell(full));
                employee.setFullName(fullName.isBlank() ? employee.getFirstName() + " " + employee.getLastName() : fullName);
                employee.setGender(gender < 0 ? null : ExcelSupport.text(row.getCell(gender)));
                if (post >= 0 && !ExcelSupport.text(row.getCell(post)).isBlank())
                    employee.setPost(referencePost(ExcelSupport.text(row.getCell(post))));
                if (location >= 0 && !ExcelSupport.text(row.getCell(location)).isBlank())
                    employee.setWorkLocation(referenceLocation(ExcelSupport.text(row.getCell(location))));
                repository.save(employee);
                count++;
            }
            return count;
        } catch (IOException | RuntimeException e) {
            if (e instanceof ExcelImportException importException) throw importException;
            throw new ExcelImportException("Unable to import Employee Excel file", e);
        }
    }

    private Post referencePost(String name) {
        return postRepository.findByName(name).orElseGet(() -> {
            Post p = new Post();
            p.setName(name);
            return postRepository.save(p);
        });
    }

    private WorkLocation referenceLocation(String name) {
        return locationRepository.findByName(name).orElseGet(() -> {
            WorkLocation p = new WorkLocation();
            p.setName(name);
            return locationRepository.save(p);
        });
    }
}
