package mp.tfg.mycheckpoint.mapper.friendship; // O mp.tfg.mycheckpoint.mapper.friendship

import mp.tfg.mycheckpoint.dto.friendship.FriendshipResponseDTO;
import mp.tfg.mycheckpoint.entity.Friendship;
import mp.tfg.mycheckpoint.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {

    @Mapping(target = "friendshipId", source = "friendship.internalId")
    @Mapping(target = "userPublicId", expression = "java(getOtherUser(friendship, currentUser).getPublicId())")
    @Mapping(target = "username", expression = "java(getOtherUser(friendship, currentUser).getNombreUsuario())")
    @Mapping(target = "profilePictureUrl", expression = "java(getOtherUser(friendship, currentUser).getFotoPerfil())")
    @Mapping(target = "status", source = "friendship.status")
    @Mapping(target = "isInitiatedByCurrentUser", expression = "java(friendship.getRequester().getId().equals(currentUser.getId()))")
    @Mapping(target = "createdAt", source = "friendship.createdAt")
    @Mapping(target = "updatedAt", source = "friendship.updatedAt")
    FriendshipResponseDTO toDto(Friendship friendship, @Context User currentUser);

    default User getOtherUser(Friendship friendship, User currentUser) {
        if (friendship.getRequester().getId().equals(currentUser.getId())) {
            return friendship.getReceiver();
        } else {
            return friendship.getRequester();
        }
    }
}
