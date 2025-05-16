package com.victor.filestorage_service.controller;

import com.victor.filestorage_service.service.GCPStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/file-storage")
@RequiredArgsConstructor
@Slf4j
public class GCPStorageController {
    private final GCPStorageService GCPStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImageToFIleSystem(@RequestPart("image") MultipartFile file) {
        log.info("Inside FileStorageController.uploadImageToFIleSystem");
        return ResponseEntity.ok().body(GCPStorageService.uploadFile(file));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String id) {
        log.info("Inside FileStorageController.downloadImageFromFileSystem with ID: {} ", id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/png"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(GCPStorageService.downloadFile(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteImageFromFileSystem(@PathVariable String id) {
        log.info("Inside FileStorageController.deleteImageFromFileSystem with ID: {} ", id);
        GCPStorageService.deleteFile(id);
        return ResponseEntity.ok().build();
    }
}
