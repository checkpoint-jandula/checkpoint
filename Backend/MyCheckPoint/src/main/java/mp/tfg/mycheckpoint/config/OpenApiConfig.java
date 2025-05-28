package mp.tfg.mycheckpoint.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.application.name:MyCheckPoint API}") String appName,
                                 @Value("${app.api.version:v1}") String appVersion,
                                 @Value("${app.api.description:API para la aplicación MyCheckPoint}") String appDescription) {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info().title(appName)
                        .version(appVersion)
                        .description(appDescription)
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                                // AÑADIR ESTOS ESQUEMAS GLOBALES:
                                .addSchemas("ErrorResponse", new ObjectSchema() // Usar ObjectSchema si es un objeto simple
                                        .type("object")
                                        .addProperty("error", new StringSchema().description("Mensaje detallado del error.").example("El recurso solicitado no fue encontrado."))
                                        .description("Respuesta de error genérica para errores 4xx y 5xx.")
                                )
                                .addSchemas("ValidationErrorResponse", new ObjectSchema()
                                        .type("object")
                                        .addProperty("errors", new ArraySchema().items(new StringSchema()).description("Lista de mensajes de error de validación.").example("[\"El nombre de usuario es obligatorio\", \"El email debe ser válido\"]"))
                                        .description("Respuesta específica para errores de validación de campos (HTTP 400).")
                                )
                                .addSchemas("DuplicatedResourceResponse", new ObjectSchema()
                                .type("object")
                                .addProperty("message", new StringSchema().description("Mensaje de error indicando que el recurso ya existe.").example("El usuario ya existe."))
                                .description("Respuesta específica para errores de recursos duplicados (HTTP 409).")
                                )
                                .addSchemas("UnauthorizedResponse", new ObjectSchema()
                                .type("object")
                                .addProperty("message", new StringSchema().description("Mensaje de error indicando que la autenticación falló.").example("No autorizado. Token inválido o expirado. O contraseña incorrecta"))
                                .description("Respuesta específica para errores de autorización (HTTP 401).")
                                )
                                .addSchemas("ValidationPasswordErrorResponse", new ObjectSchema()
                                        .type("object")
                                        .addProperty("errors", new ArraySchema().items(new StringSchema()).description("Lista de mensajes de error de validación.").example("[\"La contraseña no debe ser igual a la anterior\", \"La nueva contraseña debe tener entre 8 y 100 caracteres\"]"))
                                        .description("Respuesta específica para errores de validación de campos (HTTP 400).")
                                )
                                .addSchemas("RequiredErrorResponse", new ObjectSchema()
                                        .type("object")
                                        .addProperty("errors", new ArraySchema().items(new StringSchema()).description("Mensaje de error generico").example("El recurso es obligatorio."))
                                        .description("Respuesta específica para errores de validación de campos (HTTP 400).")
                                )
                                .addSchemas("TooLargeResponse", new ObjectSchema()
                                        .type("object")
                                        .addProperty("errors", new ArraySchema().items(new StringSchema()).description("Mensaje de error para imagenes").example("El recurso es demasiado grande. El tamaño máximo permitido es de 500KB."))
                                        .description("Respuesta específica para errores de validación de campos (HTTP 413).")
                                )
                );
    }
}
