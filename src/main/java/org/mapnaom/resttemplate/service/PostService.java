package org.mapnaom.resttemplate.service;

import org.mapnaom.resttemplate.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PostService {
    Post save(Post entity);
    Optional<Post> findById(Long id);
    Page<Post> findAll(Pageable pageable, Specification<Post> specification);
    List<Post> findAll();
    void deleteById(Long id);
    int importByExcel(MultipartFile file);
}
