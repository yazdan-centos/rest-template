package org.mapnaom.resttemplate.repository;

import org.mapnaom.resttemplate.entity.WorkLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long>, JpaSpecificationExecutor<WorkLocation> {
    Optional<WorkLocation> findByName(String name);
}
