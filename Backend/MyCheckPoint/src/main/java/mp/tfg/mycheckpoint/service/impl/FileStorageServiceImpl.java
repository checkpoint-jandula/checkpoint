package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.exception.FileStorageException;
import mp.tfg.mycheckpoint.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final Path profilePictureStorageLocation;
    private final long maxProfilePictureSizeBytes;
    private final List<String> allowedProfilePictureExtensions = Arrays.asList(".jpeg", ".jpg", ".png", ".gif");

    public FileStorageServiceImpl(
            @Value("${file.upload-dir.profile-pictures}") String uploadDir,
            @Value("${app.profile-picture.max-size}") String maxProfilePictureSize) {

        this.profilePictureStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.profilePictureStorageLocation);
            logger.info("Directorio de almacenamiento para fotos de perfil inicializado en: {}", this.profilePictureStorageLocation.toString());
        } catch (Exception ex) {
            logger.error("No se pudo crear el directorio de almacenamiento para fotos de perfil: {}", this.profilePictureStorageLocation.toString(), ex);
            // Este es un error del servidor
            throw new FileStorageException("No se pudo crear el directorio donde se guardarán los archivos subidos.", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.maxProfilePictureSizeBytes = parseSizeToBytes(maxProfilePictureSize);
        logger.info("Límite de tamaño máximo para fotos de perfil configurado a: {} bytes ({}).", this.maxProfilePictureSizeBytes, maxProfilePictureSize);
    }

    private long parseSizeToBytes(String sizeString) {
        // ... (sin cambios en esta lógica interna)
        if (sizeString == null || sizeString.isBlank()) {
            long defaultSizeBytes = 1024L * 1024L;
            logger.warn("La propiedad 'app.profile-picture.max-size' no está configurada o está vacía. Usando un valor por defecto de {} bytes (1MB).", defaultSizeBytes);
            return defaultSizeBytes;
        }
        String upperSize = sizeString.toUpperCase().trim();
        long multiplier = 1L;

        try {
            if (upperSize.endsWith("KB")) {
                multiplier = 1024L;
                upperSize = upperSize.substring(0, upperSize.length() - 2).trim();
            } else if (upperSize.endsWith("MB")) {
                multiplier = 1024L * 1024L;
                upperSize = upperSize.substring(0, upperSize.length() - 2).trim();
            } else if (upperSize.endsWith("GB")) {
                multiplier = 1024L * 1024L * 1024L;
                upperSize = upperSize.substring(0, upperSize.length() - 2).trim();
            } else if (upperSize.endsWith("B")) {
                upperSize = upperSize.substring(0, upperSize.length() - 1).trim();
            }

            if (upperSize.matches("\\d+")) {
                return Long.parseLong(upperSize) * multiplier;
            } else {
                throw new NumberFormatException("Formato numérico inválido en la cadena de tamaño.");
            }

        } catch (NumberFormatException e) {
            long fallbackSizeBytes = 1024L * 1024L;
            logger.error("Valor de tamaño de archivo inválido en 'app.profile-picture.max-size': '{}'. Usando valor por defecto de {} bytes (1MB).", sizeString, fallbackSizeBytes, e);
            return fallbackSizeBytes;
        }
    }


    @Override
    public String storeProfilePicture(MultipartFile file, String userPublicId) {
        if (file == null || file.isEmpty()) {
            // Error del cliente
            throw new FileStorageException("No se puede guardar un archivo vacío o nulo.", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > this.maxProfilePictureSizeBytes) {
            String configuredMaxSizeReadable = convertBytesToReadableSize(this.maxProfilePictureSizeBytes);
            String actualFileSizeReadable = convertBytesToReadableSize(file.getSize());
            // Error del cliente (Payload Too Large es más específico, pero BAD_REQUEST también es aceptable)
            throw new FileStorageException("El archivo excede el tamaño máximo permitido para fotos de perfil (" +
                    configuredMaxSizeReadable + "). Tamaño del archivo subido: " +
                    actualFileSizeReadable, HttpStatus.PAYLOAD_TOO_LARGE); // O HttpStatus.BAD_REQUEST
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";

        try {
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            if (fileExtension.isBlank() || !this.allowedProfilePictureExtensions.contains(fileExtension)) {
                // Error del cliente
                throw new FileStorageException("Formato de archivo no permitido: '" + originalFilename +
                        "'. Extensiones permitidas: " + String.join(", ", this.allowedProfilePictureExtensions), HttpStatus.BAD_REQUEST);
            }
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al procesar el nombre del archivo: {}", originalFilename, e);
            // Error del cliente si el nombre del archivo es problemático
            throw new FileStorageException("No se pudo determinar la extensión del archivo de forma segura: " + originalFilename, e, HttpStatus.BAD_REQUEST);
        }

        String newFileName = userPublicId + fileExtension;

        try {
            Path targetLocation = this.profilePictureStorageLocation.resolve(newFileName).normalize();
            if (!targetLocation.startsWith(this.profilePictureStorageLocation)) {
                // Esto sería un error de lógica interna o un intento malicioso, error del servidor.
                throw new FileStorageException("No se puede guardar el archivo fuera del directorio de almacenamiento designado.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Foto de perfil guardada: {} para el usuario {}", newFileName, userPublicId);
            return newFileName;
        } catch (IOException ex) {
            logger.error("No se pudo guardar el archivo {}. Causa: {}", newFileName, ex.getMessage(), ex);
            // Error del servidor
            throw new FileStorageException("No se pudo guardar el archivo " + newFileName + ". ¡Por favor, inténtalo de nuevo!", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ... (método deleteProfilePicture y convertBytesToReadableSize sin cambios)
    @Override
    public void deleteProfilePicture(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            logger.warn("Intento de eliminar un nombre de archivo de foto de perfil nulo o vacío.");
            return;
        }
        try {
            Path filePath = this.profilePictureStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(this.profilePictureStorageLocation)) {
                logger.warn("Intento de eliminar un archivo fuera del directorio de almacenamiento designado: {}", filePath);
                return;
            }

            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                Files.delete(filePath);
                logger.info("Foto de perfil eliminada: {}", fileName);
            } else {
                logger.warn("Se intentó eliminar la foto de perfil, pero no se encontró o no es un archivo regular: {}", fileName);
            }
        } catch (IOException ex) {
            logger.error("No se pudo eliminar la foto de perfil {}: {}", fileName, ex.getMessage());
        }
    }

    private String convertBytesToReadableSize(long sizeBytes) {
        if (sizeBytes >= (1024L * 1024L)) {
            return String.format("%.2fMB", (double) sizeBytes / (1024L * 1024L));
        } else if (sizeBytes >= 1024L) {
            return String.format("%.2fKB", (double) sizeBytes / 1024L);
        } else {
            return sizeBytes + "B";
        }
    }
}