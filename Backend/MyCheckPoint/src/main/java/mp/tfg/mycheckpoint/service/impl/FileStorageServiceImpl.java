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

/**
 * Implementación del servicio {@link FileStorageService} para gestionar el almacenamiento
 * de archivos, específicamente para las imágenes de perfil de los usuarios.
 * Se encarga de guardar, eliminar y validar los archivos subidos, asegurando
 * que se cumplan las restricciones de tamaño y tipo de archivo.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * Logger para esta clase.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    /**
     * Ubicación base en el sistema de archivos donde se almacenarán las imágenes de perfil.
     * Esta ruta se normaliza a una ruta absoluta durante la inicialización.
     */
    private final Path profilePictureStorageLocation;

    /**
     * Tamaño máximo permitido para las imágenes de perfil, en bytes.
     * Este valor se calcula a partir de una cadena de configuración (ej. "500KB").
     */
    private final long maxProfilePictureSizeBytes;

    /**
     * Lista de extensiones de archivo permitidas para las imágenes de perfil (ej. ".jpeg", ".png", ".gif").
     * Las extensiones se almacenan en minúsculas para una comparación insensible a mayúsculas.
     */
    private final List<String> allowedProfilePictureExtensions = Arrays.asList(".jpeg", ".jpg", ".png", ".gif");

    /**
     * Constructor para FileStorageServiceImpl.
     * Inicializa la ubicación de almacenamiento para las fotos de perfil y el tamaño máximo
     * de archivo permitido, leyendo estos valores desde las propiedades de la aplicación.
     * Crea el directorio de almacenamiento si no existe al momento de la inicialización.
     *
     * @param uploadDir La ruta del directorio de subida para las fotos de perfil,
     * inyectada desde la propiedad de la aplicación "file.upload-dir.profile-pictures".
     * @param maxProfilePictureSize El tamaño máximo permitido para las fotos de perfil,
     * expresado como una cadena (ej. "500KB", "2MB"),
     * inyectado desde la propiedad "app.profile-picture.max-size".
     * @throws FileStorageException Si no se puede crear el directorio de almacenamiento
     * configurado (error a nivel de servidor).
     */
    public FileStorageServiceImpl(
            @Value("${file.upload-dir.profile-pictures}") String uploadDir,
            @Value("${app.profile-picture.max-size}") String maxProfilePictureSize) {

        this.profilePictureStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.profilePictureStorageLocation);
            logger.info("Directorio de almacenamiento para fotos de perfil inicializado en: {}", this.profilePictureStorageLocation.toString());
        } catch (Exception ex) {
            logger.error("No se pudo crear el directorio de almacenamiento para fotos de perfil: {}", this.profilePictureStorageLocation.toString(), ex);
            // Este es un error crítico de configuración del servidor.
            throw new FileStorageException("No se pudo crear el directorio donde se guardarán los archivos subidos.", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.maxProfilePictureSizeBytes = parseSizeToBytes(maxProfilePictureSize);
        logger.info("Límite de tamaño máximo para fotos de perfil configurado a: {} bytes ({}).", this.maxProfilePictureSizeBytes, maxProfilePictureSize);
    }

    /**
     * Convierte una cadena que representa un tamaño de archivo (ej. "500KB", "2MB", "1GB")
     * a su equivalente en bytes.
     * Soporta las unidades KB, MB, GB y B (o sin sufijo para bytes).
     * Si la cadena de tamaño es nula, vacía o tiene un formato inválido,
     * se utiliza un valor por defecto de 1MB y se registra una advertencia o error.
     *
     * @param sizeString La cadena de tamaño a parsear.
     * @return El tamaño equivalente en bytes.
     */
    private long parseSizeToBytes(String sizeString) {
        // Implementación sin cambios respecto a la versión anterior...
        if (sizeString == null || sizeString.isBlank()) {
            long defaultSizeBytes = 1024L * 1024L; // 1MB por defecto
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
                if (!upperSize.matches("\\d+")) {
                    upperSize = upperSize.substring(0, upperSize.length() - 1).trim();
                }
            }

            if (upperSize.matches("\\d+")) {
                return Long.parseLong(upperSize) * multiplier;
            } else {
                throw new NumberFormatException("Formato numérico inválido en la cadena de tamaño después de procesar unidades.");
            }

        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            long fallbackSizeBytes = 1024L * 1024L;
            logger.error("Valor de tamaño de archivo inválido en 'app.profile-picture.max-size': '{}'. Usando valor por defecto de {} bytes (1MB). Error: {}", sizeString, fallbackSizeBytes, e.getMessage());
            return fallbackSizeBytes;
        }
    }

    /**
     * Almacena un archivo de imagen de perfil subido por un usuario.
     * El método realiza las siguientes validaciones antes de guardar:
     * <ol>
     * <li>Verifica que el archivo no sea nulo o vacío.</li>
     * <li>Comprueba que el tamaño del archivo no exceda el máximo configurado.</li>
     * <li>Valida que la extensión del archivo esté dentro de las permitidas (jpeg, jpg, png, gif).</li>
     * </ol>
     * El archivo se guarda con un nuevo nombre compuesto por el {@code userPublicId} y la extensión original,
     * reemplazando cualquier archivo existente con el mismo nombre.
     *
     * @param file El archivo {@link MultipartFile} que representa la imagen de perfil subida.
     * @param userPublicId El ID público (UUID en formato String) del usuario, utilizado para nombrar el archivo guardado.
     * @return El nombre del archivo generado y almacenado (ej. "uuid_del_usuario.png").
     * @throws FileStorageException Si el archivo está vacío, excede el tamaño máximo,
     * tiene un formato no permitido, no se puede determinar la extensión,
     * se intenta guardar fuera del directorio designado, o si ocurre un error de I/O.
     */
    @Override
    public String storeProfilePicture(MultipartFile file, String userPublicId) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("No se puede guardar un archivo vacío o nulo.", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > this.maxProfilePictureSizeBytes) {
            String configuredMaxSizeReadable = convertBytesToReadableSize(this.maxProfilePictureSizeBytes);
            String actualFileSizeReadable = convertBytesToReadableSize(file.getSize());
            throw new FileStorageException("El archivo excede el tamaño máximo permitido para fotos de perfil (" +
                    configuredMaxSizeReadable + "). Tamaño del archivo subido: " +
                    actualFileSizeReadable, HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";

        try {
            int lastDot = originalFilename.lastIndexOf(".");
            if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
                fileExtension = originalFilename.substring(lastDot).toLowerCase();
            }

            if (fileExtension.isBlank() || !this.allowedProfilePictureExtensions.contains(fileExtension)) {
                throw new FileStorageException("Formato de archivo no permitido: '" + originalFilename +
                        "'. Extensiones permitidas: " + String.join(", ", this.allowedProfilePictureExtensions), HttpStatus.BAD_REQUEST);
            }
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al procesar el nombre del archivo para validación de extensión: {}", originalFilename, e);
            throw new FileStorageException("No se pudo determinar la extensión del archivo de forma segura: " + originalFilename, e, HttpStatus.BAD_REQUEST);
        }

        String newFileName = userPublicId + fileExtension;

        try {
            Path targetLocation = this.profilePictureStorageLocation.resolve(newFileName).normalize();

            if (!targetLocation.startsWith(this.profilePictureStorageLocation)) {
                throw new FileStorageException("No se puede guardar el archivo fuera del directorio de almacenamiento designado.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Foto de perfil guardada: {} para el usuario {}", newFileName, userPublicId);
            return newFileName;
        } catch (IOException ex) {
            logger.error("No se pudo guardar el archivo {}. Causa: {}", newFileName, ex.getMessage(), ex);
            throw new FileStorageException("No se pudo guardar el archivo " + newFileName + ". ¡Por favor, inténtalo de nuevo!", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina un archivo de foto de perfil previamente almacenado.
     * Si el nombre del archivo es nulo, vacío, o si el archivo no se encuentra
     * o no es un archivo regular en la ubicación esperada, la operación se registra
     * con una advertencia pero no lanza una excepción.
     * También previene intentos de eliminación fuera del directorio de almacenamiento designado.
     *
     * @param fileName El nombre del archivo de la foto de perfil a eliminar (ej. "uuid_del_usuario.png").
     */
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

    /**
     * Convierte un tamaño de archivo en bytes a una representación más legible por humanos
     * utilizando las unidades B, KB, MB, GB, TB.
     *
     * @param sizeBytes El tamaño del archivo en bytes.
     * @return Una cadena formateada que representa el tamaño (ej. "1.50 MB", "500.00 KB").
     * Si {@code sizeBytes} es cero o negativo, devuelve "0 B".
     */
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