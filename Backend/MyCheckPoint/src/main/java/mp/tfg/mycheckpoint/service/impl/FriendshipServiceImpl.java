package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.enums.FriendshipStatus;
import mp.tfg.mycheckpoint.dto.friendship.FriendshipResponseDTO;
import mp.tfg.mycheckpoint.entity.Friendship;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.exception.DuplicateEntryException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.exception.UnauthorizedOperationException;
import mp.tfg.mycheckpoint.mapper.friendship.FriendshipMapper;
import mp.tfg.mycheckpoint.repository.FriendshipRepository;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.service.FriendshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipServiceImpl.class);

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper friendshipMapper;

    @Autowired
    public FriendshipServiceImpl(UserRepository userRepository,
                                 FriendshipRepository friendshipRepository,
                                 FriendshipMapper friendshipMapper) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipMapper = friendshipMapper;
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private User getUserByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }

    @Override
    @Transactional
    public FriendshipResponseDTO sendFriendRequest(String senderUserEmail, UUID receiverUserPublicId) {
        User sender = getUserByEmailOrThrow(senderUserEmail);
        User receiver = getUserByPublicIdOrThrow(receiverUserPublicId);

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud de amistad a ti mismo.");
        }

        // Verificar si ya existe una relación (amistad aceptada o solicitud pendiente en cualquier dirección)
        Optional<Friendship> existingRelation = friendshipRepository.findFriendshipBetweenUsers(sender, receiver);
        if (existingRelation.isPresent()) {
            Friendship relation = existingRelation.get();
            if (relation.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new DuplicateEntryException("Ya eres amigo de este usuario.");
            }
            if (relation.getStatus() == FriendshipStatus.PENDING) {
                if (relation.getRequester().getId().equals(sender.getId())) {
                    throw new DuplicateEntryException("Ya has enviado una solicitud de amistad a este usuario.");
                } else {
                    // MODIFICACIÓN: Auto-aceptar si B envía a A y A->B ya estaba PENDING
                    logger.info("Solicitud mutua detectada entre {} y {}. Aceptando automáticamente.", sender.getNombreUsuario(), receiver.getNombreUsuario());
                    relation.setStatus(FriendshipStatus.ACCEPTED);
                    Friendship acceptedFriendship = friendshipRepository.save(relation);
                    // Aquí podrías publicar un evento de "AmistadAceptada" para notificaciones
                    return friendshipMapper.toDto(acceptedFriendship, sender);
                }
            }
        }

        // Crear nueva solicitud
        Friendship newRequest = Friendship.builder()
                .requester(sender)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();
        Friendship savedRequest = friendshipRepository.save(newRequest);
        logger.info("Usuario {} envió solicitud de amistad a {}", sender.getNombreUsuario(), receiver.getNombreUsuario());
        // Aquí podrías publicar un evento de "SolicitudRecibida" para notificaciones
        return friendshipMapper.toDto(savedRequest, sender);
    }

    @Override
    @Transactional
    public FriendshipResponseDTO acceptFriendRequest(String currentUserEmail, UUID requesterUserPublicId) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail); // Este es el receptor de la solicitud
        User requester = getUserByPublicIdOrThrow(requesterUserPublicId);

        Friendship request = friendshipRepository.findFriendshipBetweenUsersWithStatus(requester, currentUser, FriendshipStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una solicitud de amistad pendiente de " + requester.getNombreUsuario()));

        // Asegurarse de que el currentUser es el receptor de esta solicitud específica
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedOperationException("No puedes aceptar una solicitud que no te fue enviada.");
        }

        request.setStatus(FriendshipStatus.ACCEPTED);
        Friendship acceptedFriendship = friendshipRepository.save(request);
        logger.info("Usuario {} aceptó la solicitud de amistad de {}", currentUser.getNombreUsuario(), requester.getNombreUsuario());
        // Aquí podrías publicar un evento de "AmistadAceptada"
        return friendshipMapper.toDto(acceptedFriendship, currentUser);
    }

    @Override
    @Transactional
    public void declineFriendRequest(String currentUserEmail, UUID requesterUserPublicId) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail); // Receptor
        User requester = getUserByPublicIdOrThrow(requesterUserPublicId);

        Friendship request = friendshipRepository.findFriendshipBetweenUsersWithStatus(requester, currentUser, FriendshipStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una solicitud de amistad pendiente de " + requester.getNombreUsuario()));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedOperationException("No puedes rechazar una solicitud que no te fue enviada.");
        }

        // En lugar de cambiar el estado a DECLINED, simplemente la eliminamos para no saturar la tabla.
        // Si se quisiera mantener un historial de solicitudes declinadas, se cambiaría el estado.
        friendshipRepository.delete(request);
        logger.info("Usuario {} rechazó la solicitud de amistad de {}", currentUser.getNombreUsuario(), requester.getNombreUsuario());
        // Aquí podrías publicar un evento de "SolicitudRechazada"
    }

    @Override
    @Transactional
    public void removeFriend(String currentUserEmail, UUID friendUserPublicId) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail);
        User friendToRemove = getUserByPublicIdOrThrow(friendUserPublicId);

        Friendship friendship = friendshipRepository.findFriendshipBetweenUsersWithStatus(currentUser, friendToRemove, FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una amistad con el usuario " + friendToRemove.getNombreUsuario()));

        friendshipRepository.delete(friendship);
        logger.info("Usuario {} eliminó de sus amigos a {}", currentUser.getNombreUsuario(), friendToRemove.getNombreUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDTO> getFriends(String currentUserEmail) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail);
        return friendshipRepository.findAcceptedFriendshipsForUser(currentUser, FriendshipStatus.ACCEPTED)
                .stream()
                .map(friendship -> friendshipMapper.toDto(friendship, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDTO> getPendingRequestsReceived(String currentUserEmail) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail); // Es el receptor
        return friendshipRepository.findByReceiverAndStatus(currentUser, FriendshipStatus.PENDING)
                .stream()
                .map(friendship -> friendshipMapper.toDto(friendship, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDTO> getPendingRequestsSent(String currentUserEmail) {
        User currentUser = getUserByEmailOrThrow(currentUserEmail); // Es el solicitante
        return friendshipRepository.findByRequesterAndStatus(currentUser, FriendshipStatus.PENDING)
                .stream()
                .map(friendship -> friendshipMapper.toDto(friendship, currentUser))
                .collect(Collectors.toList());
    }
}