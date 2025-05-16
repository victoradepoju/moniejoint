package com.victor.filestorage_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FileMetadata {
    @Id
    private String id;
    private String name;
    private String contentType;
    private String gcsPath;
    private Long size;
}
