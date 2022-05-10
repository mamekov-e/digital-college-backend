package com.college.backend.service;

import com.college.backend.exceptions.FileAlreadyExistException;
import com.college.backend.exceptions.FileNotFoundException;
import com.college.backend.exceptions.ImageNotFoundException;
import com.college.backend.model.FileModel;
import com.college.backend.model.User;
import com.college.backend.repository.FileRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.nio.file.Paths.get;

@Service
public class FileService {
    public static final Logger LOG = LoggerFactory.getLogger(ImageService.class);

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Autowired
    public FileService(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    public FileModel uploadFileToUser(MultipartFile file, String uploadType, Principal principal) throws IOException {

        // чтобы загрузить файл для аттестата или документа подтверждающего личность
        // используется одна таблица file_model, в которой есть поле type,
        // отвечающий за загрузка файла в зависимости от назначения.
        // Типы: id-scan и attestation-scan

        User user = getUserByPrincipal(principal);
        LOG.info("Uploading file {}", user.getEmail());

        // получаем по id пользователя и по типу содержимое файла с аттрибутами из бд
        // записываем в userFile если есть, иначе начначаем его пустым
        FileModel userFile = fileRepository.findByUserIdAndType(user.getId(), uploadType)
                .orElse(null);

        // также чтобы проверять по той же айди пользователя и типу нужно получить содержимое
        // второго файла с этим же айди пользователя, но с другим типом
        String uploadAnotherType;

        // если переданная переменная uploadType равна id-scan
        // то логично записать другой тип - attestation-scan
        // и наоборот
        if (uploadType.equals(SecurityConstants.ID_SCAN)) {
            uploadAnotherType = SecurityConstants.ATTESTATION_SCAN;
        } else
            uploadAnotherType = SecurityConstants.ID_SCAN;

        // затем переменную с другим типом мы используем чтобы вернуть из бд
        // если есть записываем, если нет то null
        FileModel userFileAnotherType = fileRepository.findByUserIdAndType(
                        user.getId(), uploadAnotherType)
                .orElse(null);

        // в случае если все таки оба типа файла уже добавления то нужно сравнить название загружаемого файла
        // и если совпадает, бросить исключение с текстом, что имя загружаемого файла уже существует
        if (userFileAnotherType != null && userFileAnotherType.getName().equals(file.getOriginalFilename())) {
            throw new FileAlreadyExistException(file.getOriginalFilename() + " file with this name already exist");
        }

        // в этом условии идет проверка на то,
        // чтобы один пользователь мог добавить только один экземпляр каждого типа
        // если добавляемый тип файла уже существует надо его удалить и загрузить текущий
        if (!ObjectUtils.isEmpty(userFile)) {
            fileRepository.delete(userFile);
        }

        // пройдя все проверки в конце остается создать модель класса файла
        // и заполнить аттрибуты чтобы сохранить бд
        FileModel fileModel = new FileModel();
        fileModel.setUserId(user.getId());
        fileModel.setType(uploadType);
        fileModel.setFileType(file.getContentType());
        fileModel.setFileByte(compressBytes(file.getBytes()));
        fileModel.setName(file.getOriginalFilename());

        LOG.info("File uploaded {}", fileModel.getName());
        return fileRepository.save(fileModel);
    }

    public FileModel downloadUserFile(String id, String filename) {
        Long userId = Long.parseLong(id);

        FileModel fileModel = fileRepository.findByUserIdAndName(userId, filename)
                .orElseThrow(() -> new FileNotFoundException("File " + filename + " not found for user id: " + userId));

        if (!ObjectUtils.isEmpty(fileModel)) {
            fileModel.setFileByte(decompressBytes(fileModel.getFileByte()));
        }

        return fileModel;
    }

    public List<FileModel> getUserIdFiles() {
        return fileRepository.findAll().stream()
                .filter((file) -> file.getType().equals(SecurityConstants.ID_SCAN))
                .collect(Collectors.toList());
    }

    public List<FileModel> getUserAttestationFiles() {
        return fileRepository.findAll().stream()
                .filter((file) -> file.getType().equals(SecurityConstants.ATTESTATION_SCAN))
                .collect(Collectors.toList());
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
        System.out.println("Compressed File Byte Size - " + outputStream.toByteArray().length);
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
