package mp.tfg.mycheckpoint.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MvcConfig.class);

    @Value("${file.resource-handler.profile-pictures}")
    private String profilePicturesResourceHandler; // ej: /profile-pictures/**

    @Value("${file.resource-locations.profile-pictures}")
    private String profilePicturesResourceLocations; // ej: file:./uploads/profile-pictures/

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir fotos de perfil
        exposeDirectory(profilePicturesResourceHandler, profilePicturesResourceLocations, registry);
        // Puedes añadir más handlers para otros tipos de archivos estáticos si es necesario
    }

    private void exposeDirectory(String pathPattern, String fileSystemPath, ResourceHandlerRegistry registry) {
        if (pathPattern == null || fileSystemPath == null) {
            logger.warn("La configuración para el resource handler (pathPattern o fileSystemPath) es nula. No se configurará.");
            return;
        }

        Path uploadDir = Paths.get(fileSystemPath.replace("file:", ""));
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        logger.info("Configurando resource handler: {} -> {}", pathPattern, "file:" + uploadPath + "/");

        registry.addResourceHandler(pathPattern)
                .addResourceLocations("file:" + uploadPath + "/");
    }
}