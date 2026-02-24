package com.speakBuddy.speackBuddy_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interfaz para servicios de almacenamiento de archivos.
 * Permite cambiar fácilmente entre almacenamiento local y cloud (S3, Firebase, etc.)
 */
public interface StorageService {

    /**
     * Sube un archivo y devuelve la URL pública para acceder a él.
     *
     * @param file     El archivo a subir
     * @param folder   Carpeta donde guardar (ej: "avatars", "documents")
     * @return URL pública del archivo subido
     * @throws IOException si ocurre un error al guardar el archivo
     */
    String upload(MultipartFile file, String folder) throws IOException;

    /**
     * Elimina un archivo por su URL.
     *
     * @param fileUrl URL del archivo a eliminar
     */
    void delete(String fileUrl);

    /**
     * Comprueba si un archivo existe.
     *
     * @param fileUrl URL del archivo
     * @return true si existe, false en caso contrario
     */
    boolean exists(String fileUrl);
}
