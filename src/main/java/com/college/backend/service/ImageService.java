package com.college.backend.service;

import com.college.backend.exceptions.ImageNotFoundException;
import com.college.backend.model.ImageModel;
import com.college.backend.model.User;
import com.college.backend.repository.ImageRepository;
import com.college.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class ImageService {
    public static final Logger LOG = LoggerFactory.getLogger(ImageService.class);

    private UserRepository userRepository;
    private ImageRepository imageRepository;

    @Autowired
    public ImageService(UserRepository userRepository, ImageRepository imageRepository) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    public ImageModel uploadImageToUser(MultipartFile file, Principal principal) throws IOException {
        User user = getUserByPrincipal(principal);
        LOG.info("Uploading image profile to User {}", user.getEmail());

        ImageModel userProfileImage = imageRepository.findByUserId(user.getId()).orElse(null);
        if (!ObjectUtils.isEmpty(userProfileImage)) {
            imageRepository.delete(userProfileImage);
        }

        ImageModel imageModel = new ImageModel();
        imageModel.setUserId(user.getId());
        imageModel.setImageByte(compressBytes(file.getBytes()));
        imageModel.setName(file.getOriginalFilename());

        LOG.info("Image uploaded {}", imageModel.getName());
        return imageRepository.save(imageModel);
    }

    public ImageModel getImageToUser(Principal principal) {
        User user = getUserByPrincipal(principal);
        Long userId = user.getId();
        String email = user.getEmail();

        ImageModel imageModel = imageRepository.findByUserId(userId)
                .orElseThrow( () -> new ImageNotFoundException("Image not found for user: " + email));

        if (!ObjectUtils.isEmpty(imageModel)) {
            imageModel.setImageByte(decompressBytes(imageModel.getImageByte()));
        }

        return imageModel;
    }

    private byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            LOG.error("Cannot compress Bytes");
        }
        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);
        return outputStream.toByteArray();
    }

    private static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException e) {
            LOG.error("Cannot decompress Bytes");
        }
        return outputStream.toByteArray();
    }

    private User getUserByPrincipal(Principal principal) {
        String email = principal.getName();

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User email not found: " + email));
    }
}
