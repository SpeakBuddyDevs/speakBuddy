package com.speakBuddy.speackBuddy_backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementación de StorageService para almacenamiento local.
 * Los archivos se guardan en el sistema de archivos del servidor.
 * 
 * Para migrar a cloud (S3, Firebase, etc.), crear una nueva implementación
 * de StorageService y cambiar el @Service activo mediante @Profile o @Primary.
 */
@Service
public class LocalStorageService implements StorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:/uploads}")
    private String baseUrl;

    /**
     * Crea el directorio de uploads si no existe al iniciar la aplicación.
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + uploadDir, e);
        }
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Crear subcarpeta si se especifica
        Path targetDir = Paths.get(uploadDir);
        if (folder != null && !folder.isEmpty()) {
            targetDir = targetDir.resolve(folder);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
        }

        // Generar nombre único para evitar colisiones
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path targetPath = targetDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Construir y devolver URL
        String relativePath = folder != null && !folder.isEmpty()
                ? folder + "/" + uniqueFilename
                : uniqueFilename;

        return baseUrl + "/" + relativePath;
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extraer ruta relativa de la URL
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log error pero no lanzar excepción para no interrumpir operaciones
            System.err.println("Error al eliminar archivo: " + fileUrl + " - " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String relativePath = fileUrl.replace(baseUrl + "/", "");
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.exists(filePath);
    }
}
