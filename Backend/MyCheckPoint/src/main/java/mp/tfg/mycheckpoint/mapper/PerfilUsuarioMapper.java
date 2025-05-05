package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named; // Necesario para @Named en métodos personalizados

import mp.tfg.mycheckpoint.dto.perfilusuario.PerfilUsuarioDTO;
import mp.tfg.mycheckpoint.dto.perfilusuario.PerfilUsuarioUpdateDTO;
import mp.tfg.mycheckpoint.entity.PerfilUsuario;

import java.util.Base64; // Para manejar Base64

@Mapper(componentModel = "spring")
public interface PerfilUsuarioMapper {

    // Mapea de Entidad PerfilUsuario a PerfilUsuarioDTO
    // Mapeamos el ID del usuario asociado y convertimos byte[] a String Base64
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "fotoPerfil", target = "fotoPerfil", qualifiedByName = "bytesToBase64") // Usa el método personalizado
    PerfilUsuarioDTO toDTO(PerfilUsuario perfilUsuario);

    // Mapea de DTO de actualización a Entidad PerfilUsuario existente
    // Ignoramos el ID del perfil, el usuario, la foto de perfil y las fechas.
    // MapStruct actualizará los campos existentes en la entidad con los valores del DTO.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true) // La relación con Usuario no se actualiza por este DTO
    @Mapping(target = "fotoPerfil", ignore = true) // La foto se actualiza por separado
    @Mapping(target = "fechaCreacion", ignore = true) // Gestionado por DB
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaEliminacion", ignore = true) // Gestionado por soft delete
    void updateEntityFromDTO(PerfilUsuarioUpdateDTO perfilUsuarioUpdateDTO, @MappingTarget PerfilUsuario perfilUsuario);

    // Método personalizado para convertir byte[] a String Base64
    // @Named permite referenciar este método en @Mapping(qualifiedByName = "...")
    @Named("bytesToBase64")
    default String bytesToBase64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Si necesitaras un método para convertir de Base64 a byte[] (ej. si recibieras Base64 en PUT)
    // @Named("base64ToBytes")
    // default byte[] base64ToBytes(String base64) {
    //     if (base64 == null) {
    //         return null;
    //     }
    //     return Base64.getDecoder().decode(base64);
    // }
}