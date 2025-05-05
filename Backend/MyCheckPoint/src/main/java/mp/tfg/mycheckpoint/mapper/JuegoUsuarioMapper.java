package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import mp.tfg.mycheckpoint.mapper.JuegoMapper;
import mp.tfg.mycheckpoint.dto.juegousuario.JuegoUsuarioCreateDTO;
import mp.tfg.mycheckpoint.dto.juegousuario.JuegoUsuarioDetalleDTO;
import mp.tfg.mycheckpoint.dto.juegousuario.JuegoUsuarioDTO;
import mp.tfg.mycheckpoint.dto.juegousuario.JuegoUsuarioUpdateDTO;
import mp.tfg.mycheckpoint.mapper.PlataformaMapper;
import mp.tfg.mycheckpoint.entity.JuegoUsuario;


@Mapper(componentModel = "spring", uses = { JuegoMapper.class, PlataformaMapper.class })
public interface JuegoUsuarioMapper {

    JuegoUsuarioDTO toDTO(JuegoUsuario juegoUsuario);

    @Mapping(target = "juego", source = "juego")
    @Mapping(target = "plataforma", source = "plataforma")
    JuegoUsuarioDetalleDTO toDetalleDTO(JuegoUsuario juegoUsuario);

    // Mapea de DTO de creación a Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true) // <--- Ignorar: El usuario se asigna en el servicio
    @Mapping(target = "juego", ignore = true) // <--- Ignorar: El juego se busca y asigna en el servicio
    @Mapping(target = "plataforma", ignore = true) // <--- Ignorar: La plataforma se busca y asigna en el servicio
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "importado", ignore = true)
    // @Mapping(source = "juegoId", target = "juego.id") // <--- ELIMINAR o COMENTAR: MapStruct no puede hacer esto
    JuegoUsuario toEntity(JuegoUsuarioCreateDTO juegoUsuarioCreateDTO);

    // Mapea de DTO de actualización a Entidad existente
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true) // <--- Ignorar
    @Mapping(target = "juego", ignore = true) // <--- Ignorar
    @Mapping(target = "plataforma", ignore = true) // <--- Ignorar: La plataforma se actualiza via plataformaId en el DTO y buscar/asignar en el servicio
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "importado", ignore = true)
    void updateEntityFromDTO(JuegoUsuarioUpdateDTO juegoUsuarioUpdateDTO, @MappingTarget JuegoUsuario juegoUsuario);
}