package com.college.backend.controller;

import com.college.backend.model.FileModel;
import com.college.backend.payload.response.MessageResponse;
import com.college.backend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@RequestMapping("api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload/{uploadType}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> uploadFile(@RequestParam MultipartFile file,
                                                      @PathVariable("uploadType") String uploadType,
                                                      Principal principal) throws IOException {
        FileModel fileSaved = fileService.uploadFileToUser(file, uploadType, principal);

        return ResponseEntity.ok(new MessageResponse("File " + fileSaved.getName() + " uploaded successfully"));
    }

    @GetMapping("/download/{filename}/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename,
                                                 @PathVariable("userId") String userId) {

        FileModel file = fileService.downloadUserFile(userId, filename);

        MediaType contentType = MediaType.parseMediaType(file.getFileType());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", file.getName());
        httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + file.getName());

        return ResponseEntity.ok().contentType(contentType).headers(httpHeaders)
                .body(new ByteArrayResource(file.getFileByte()));
    }

    @GetMapping("/get-all-id-scans")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FileModel> getAllIdScans() {
        return fileService.getUserIdFiles();
    }


    @GetMapping("/get-all-attestation-scans")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FileModel> getAllAttestationScans() {
        return fileService.getUserAttestationFiles();
    }
}
