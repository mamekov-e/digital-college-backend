package com.college.backend.repository;

import com.college.backend.model.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileModel, Long> {
        Optional<FileModel> findByUserIdAndName(Long userId, String name);
        Optional<FileModel> findByUserIdAndType(Long userId, String type);
}

