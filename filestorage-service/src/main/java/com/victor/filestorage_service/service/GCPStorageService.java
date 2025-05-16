package com.victor.filestorage_service.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.victor.filestorage_service.exception.StorageException;
import com.victor.filestorage_service.model.FileMetadata;
import com.victor.filestorage_service.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GCPStorageService {
    private final Storage storage;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        try {
            String uuid = UUID.randomUUID().toString();
            log.info("uuid: {}", uuid);
            String originalFilename = file.getOriginalFilename();
            log.info("originalFilename: {}", originalFilename);
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            log.info("extension: {}", extension);
            String gcsFileName = uuid + extension;
            log.info("gcsFileName: {}", gcsFileName);

            BlobId blobId = BlobId.of(bucketName, gcsFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // Generate public URL
            String publicUrl = generatePublicUrl(gcsFileName);
            log.info("publicUrl: {}", publicUrl);

            FileMetadata metadata = FileMetadata.builder()
                    .id(uuid)
                    .name(originalFilename)
                    .contentType(file.getContentType())
                    .gcsPath(publicUrl)
                    .build();

            fileMetadataRepository.save(metadata);

            return publicUrl;
        } catch (IOException e) {
            throw new StorageException("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] downloadFile(String fileId) {
        FileMetadata metadata = findFileMetadata(fileId);
        Blob blob = storage.get(bucketName, extractGcsFileName(metadata.getGcsPath()));

        if (blob == null) {
            throw new StorageException("File not found in storage", HttpStatus.NOT_FOUND);
        }

        return blob.getContent();
    }

    public void deleteFile(String fileId) {
        FileMetadata metadata = findFileMetadata(fileId);

        String gcsFileName = extractGcsFileName(metadata.getGcsPath());

        boolean deleted = storage.delete(bucketName, gcsFileName);
        if (!deleted) {
            throw new StorageException("Failed to delete file from storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        fileMetadataRepository.deleteById(fileId);
    }

    public String generatePublicUrl(String gcsFileName) {
        return "https://storage.cloud.google.com/" + bucketName + "/" + gcsFileName;
    }

    private FileMetadata findFileMetadata(String fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new StorageException("File not found", HttpStatus.NOT_FOUND));
    }

    private String extractGcsFileName(String gcsPath) {
        return gcsPath.substring(gcsPath.lastIndexOf("/") + 1);
    }

    public String extractFileIdFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
    }
}
