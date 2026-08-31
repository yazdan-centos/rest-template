package org.mapnaom.resttemplate.service;

import org.mapnaom.resttemplate.entity.WorkLocation;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface WorkLocationService {
    WorkLocation save(WorkLocation entity);
    Optional<WorkLocation> findById(Long id);
    Page<WorkLocation> findAll(Pageable pageable, Specification<WorkLocation> specification);
    List<WorkLocation> findAll();
    void deleteById(Long id);
    int importByExcel(MultipartFile file);
}
