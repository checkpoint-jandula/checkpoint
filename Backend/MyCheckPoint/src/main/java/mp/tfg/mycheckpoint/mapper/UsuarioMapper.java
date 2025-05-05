package mp.tfg.mycheckpoint.mapper;

// Importa las anotaciones de la librería de mapeo que elijas, por ejemplo, MapStruct
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import mp.tfg.mycheckpoint.dto.usuario.UsuarioCreateDTO;
import mp.tfg.mycheckpoint.dto.usuario.UsuarioDTO;
import mp.tfg.mycheckpoint.dto.usuario.UsuarioSummaryDTO;
import mp.tfg.mycheckpoint.dto.usuario.UsuarioUpdateDTO;
import mp.tfg.mycheckpoint.entity.Usuario;

@Mapper(componentModel = "spring") // Configuración para MapStruct con Spring
public interface UsuarioMapper {

    // UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class); // Para usar sin inyección de Spring

    // Mapea de Entidad a DTO completo
    UsuarioDTO toDTO(Usuario usuario);

    // Mapea de Entidad a DTO resumen
    UsuarioSummaryDTO toSummaryDTO(Usuario usuario);

    // Mapea de DTO de creación a Entidad
    @Mapping(target = "id", ignore = true) // Ignora el ID al crear
    @Mapping(target = "publicId", ignore = true) // Se generará en la entidad o DB
    @Mapping(target = "fechaRegistro", ignore = true) // Se generará en la entidad o DB
    @Mapping(target = "fechaCreacion", ignore = true) // Se generará en la entidad o DB
    @Mapping(target = "fechaModificacion", ignore = true) // Se generará en la entidad o DB
    @Mapping(target = "fechaEliminacion", ignore = true) // Se gestiona por soft delete
    @Mapping(target = "perfilUsuario", ignore = true) // Se creará por separado o después
    @Mapping(target = "juegosUsuario", ignore = true) // Colecciones se gestionan por separado
    // ... (ignorar todas las colecciones y campos autogenerados/gestionados por la DB)
    Usuario toEntity(UsuarioCreateDTO usuarioCreateDTO);

    // Mapea de DTO de actualización a una Entidad existente
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB o UpdateTimestamp
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "contrasena", ignore = true) // La contraseña se gestiona aparte (hasheo)
    // ... (ignorar todas las colecciones y campos autogenerados/gestionados por la DB)
    void updateEntityFromDTO(UsuarioUpdateDTO usuarioUpdateDTO, @MappingTarget Usuario usuario); // MapStruct actualiza la entidad existente
}