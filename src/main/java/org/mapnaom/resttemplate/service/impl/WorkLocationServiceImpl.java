package org.mapnaom.resttemplate.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.mapnaom.resttemplate.entity.WorkLocation;
import org.mapnaom.resttemplate.repository.WorkLocationRepository;
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
public class WorkLocationServiceImpl implements WorkLocationService {
    private final WorkLocationRepository repository;
    public WorkLocationServiceImpl(WorkLocationRepository repository) { this.repository = repository; }
    public WorkLocation save(WorkLocation entity) { return repository.save(entity); }
    @Transactional(readOnly = true) public Optional<WorkLocation> findById(Long id) { return repository.findById(id); }
    @Transactional(readOnly = true) public Page<WorkLocation> findAll(Pageable pageable, Specification<WorkLocation> specification) {
        return specification == null ? repository.findAll(pageable) : repository.findAll(specification, pageable);
    }
    @Transactional(readOnly = true) public List<WorkLocation> findAll() { return repository.findAll(); }
    public void deleteById(Long id) { repository.deleteById(id); }
    public int importByExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ExcelImportException("Excel file is empty");
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw new ExcelImportException("Excel header is missing");
            int nameCol = ExcelSupport.column(header, "name", "نام");
            int descCol = ExcelSupport.column(header, "description", "توضیحات");
            if (nameCol < 0) throw new ExcelImportException("Required column 'name' is missing");
            int count = 0;
            for (int i = header.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i); if (row == null || ExcelSupport.text(row.getCell(nameCol)).isBlank()) continue;
                String name = ExcelSupport.required(row, nameCol, "name");
                WorkLocation location = repository.findByName(name).orElseGet(WorkLocation::new);
                location.setName(name); location.setDescription(descCol < 0 ? null : ExcelSupport.text(row.getCell(descCol)));
                repository.save(location); count++;
            }
            return count;
        } catch (IOException | RuntimeException e) {
            if (e instanceof ExcelImportException importException) throw importException;
            throw new ExcelImportException("Unable to import WorkLocation Excel file", e);
        }
    }
}
