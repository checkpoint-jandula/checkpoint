package mp.tfg.mycheckpoint.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Guarda el archivo de imagen de perfil.
     *
     * @param file El archivo MultipartFile subido.
     * @param userPublicId El UUID público del usuario, usado para nombrar el archivo o subdirectorio.
     * @return El nombre del archivo (o ruta relativa) que se almacenará en la base de datos.
     */
    String storeProfilePicture(MultipartFile file, String userPublicId);

    /**
     * Elimina una foto de perfil existente.
     * @param fileName El nombre del archivo a eliminar.
     */
    void deleteProfilePicture(String fileName);

    // Podrías añadir métodos para cargar/descargar archivos si fueran necesarios
    // Resource loadFileAsResource(String fileName);
}