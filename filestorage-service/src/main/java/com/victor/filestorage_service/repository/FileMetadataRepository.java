package com.victor.filestorage_service.repository;

import com.victor.filestorage_service.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
}
