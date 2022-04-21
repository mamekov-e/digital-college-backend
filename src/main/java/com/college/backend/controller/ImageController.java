package com.college.backend.controller;

import com.college.backend.model.ImageModel;
import com.college.backend.payload.response.MessageResponse;
import com.college.backend.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@CrossOrigin
@RestController
@RequestMapping("api/image")
@PreAuthorize("hasRole('USER')")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<MessageResponse> uploadImageToUser(@RequestParam MultipartFile file,
                                                             Principal principal) throws IOException {
        ImageModel imageModel = imageService.uploadImageToUser(file, principal);
        return ResponseEntity.ok(new MessageResponse("Image " + imageModel.getName() + " uploaded successfully"));
    }

    @GetMapping("/profile-image")
    public ResponseEntity<ImageModel> getUserImage(Principal principal) {
        ImageModel imageModel = imageService.getImageToUser(principal);
        
        return new ResponseEntity<>(imageModel, HttpStatus.OK);
    }
}
