package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.enums.TierListType;
import mp.tfg.mycheckpoint.dto.tierlist.*;
import mp.tfg.mycheckpoint.entity.*;
import mp.tfg.mycheckpoint.exception.DuplicateEntryException;
import mp.tfg.mycheckpoint.exception.InvalidOperationException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.exception.UnauthorizedOperationException;
import mp.tfg.mycheckpoint.mapper.tierlist.TierListMapper;
import mp.tfg.mycheckpoint.repository.*;
import mp.tfg.mycheckpoint.service.TierListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TierListServiceImpl implements TierListService {

    private static final Logger logger = LoggerFactory.getLogger(TierListServiceImpl.class);
    private static final int MAX_USER_DEFINED_SECTIONS = 8;
    private static final int MIN_USER_DEFINED_SECTIONS = 1;
    private static final String UNCLASSIFIED_SECTION_NAME = "Juegos por Clasificar";
    private static final int UNCLASSIFIED_SECTION_ORDER = 0;
    private static final int USER_SECTION_START_ORDER = 1;

    private final UserRepository userRepository;
    private final GameListRepository gameListRepository;
    private final UserGameRepository userGameRepository;
    private final TierListRepository tierListRepository;
    private final TierSectionRepository tierSectionRepository;
    private final TierListItemRepository tierListItemRepository;
    private final TierListMapper tierListMapper;

    @Autowired
    public TierListServiceImpl(UserRepository userRepository,
                               GameListRepository gameListRepository,
                               UserGameRepository userGameRepository,
                               TierListRepository tierListRepository,
                               TierSectionRepository tierSectionRepository,
                               TierListItemRepository tierListItemRepository,
                               TierListMapper tierListMapper) {
        this.userRepository = userRepository;
        this.gameListRepository = gameListRepository;
        this.userGameRepository = userGameRepository;
        this.tierListRepository = tierListRepository;
        this.tierSectionRepository = tierSectionRepository;
        this.tierListItemRepository = tierListItemRepository;
        this.tierListMapper = tierListMapper;
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private TierList findTierListByPublicIdAndOwnerOrThrow(UUID publicId, User owner) {
        return tierListRepository.findByPublicIdAndOwnerWithDetails(publicId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("TierList con ID público " + publicId + " no encontrada para el usuario " + owner.getNombreUsuario()));
    }

    private TierList findTierListByPublicIdForReadOrThrow(UUID publicId, User currentUser) {
        TierList tierList = tierListRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("TierList no encontrada con ID público: " + publicId));

        // CORREGIDO AQUÍ: usar getId() para User
        if (!tierList.isPublic() && (currentUser == null || !tierList.getOwner().getId().equals(currentUser.getId()))) {
            throw new UnauthorizedOperationException("No tienes permiso para acceder a esta TierList.");
        }

        if (tierList.isPublic()) {
            return tierListRepository.findByPublicIdAndIsPublicTrueWithDetails(publicId)
                    .orElseThrow(() -> new ResourceNotFoundException("TierList pública no encontrada o inaccesible con ID: " + publicId));
        } else {
            return tierListRepository.findByPublicIdAndOwnerWithDetails(publicId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("TierList privada no encontrada o inaccesible con ID: " + publicId));
        }
    }


    @Override
    @Transactional
    public TierListResponseDTO createProfileTierList(String userEmail, TierListCreateRequestDTO createRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);

        TierList tierList = TierList.builder()
                .owner(owner)
                .name(createRequestDTO.getName())
                .description(createRequestDTO.getDescription())
                .isPublic(createRequestDTO.getIsPublic() != null ? createRequestDTO.getIsPublic() : false)
                .type(TierListType.PROFILE_GLOBAL)
                .build();

        createDefaultSections(tierList);
        TierList savedTierList = tierListRepository.save(tierList);
        logger.info("TierList de perfil '{}' creada para el usuario {}", savedTierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(savedTierList);
    }

    private void createDefaultSections(TierList tierList) {
        TierSection unclassifiedSection = TierSection.builder()
                .tierList(tierList)
                .name(UNCLASSIFIED_SECTION_NAME)
                .sectionOrder(UNCLASSIFIED_SECTION_ORDER)
                .isDefaultUnclassified(true)
                .build();
        tierList.addSection(unclassifiedSection);

        String[] defaultNames = {"S", "A", "B", "C", "D"};
        for (int i = 0; i < defaultNames.length; i++) {
            TierSection section = TierSection.builder()
                    .tierList(tierList)
                    .name(defaultNames[i])
                    .sectionOrder(USER_SECTION_START_ORDER + i)
                    .isDefaultUnclassified(false)
                    .build();
            tierList.addSection(section);
        }
    }

    @Override
    @Transactional
    public TierListResponseDTO getOrCreateTierListForGameList(String userEmail, UUID gameListPublicId) {
        User currentUser = getUserByEmailOrThrow(userEmail);
        GameList sourceGameList = gameListRepository.findByPublicId(gameListPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("GameList no encontrada con ID público: " + gameListPublicId));

        // CORREGIDO AQUÍ: usar getId() para User
        if (!sourceGameList.getOwner().getId().equals(currentUser.getId()) && !sourceGameList.isPublic()) {
            throw new UnauthorizedOperationException("No tienes permiso para acceder a esta GameList.");
        }

        Optional<TierList> existingTierListOpt = tierListRepository.findBySourceGameListAndType(sourceGameList, TierListType.FROM_GAMELIST);

        TierList tierList;
        if (existingTierListOpt.isPresent()) {
            tierList = existingTierListOpt.get();
            logger.debug("TierList existente encontrada para GameList {}. Sincronizando juegos...", gameListPublicId);
        } else {
            logger.info("Creando nueva TierList para GameList {} ({})", sourceGameList.getName(), gameListPublicId);
            tierList = TierList.builder()
                    .owner(sourceGameList.getOwner())
                    .name(sourceGameList.getName())
                    .description("Tier list para la lista de juegos: " + sourceGameList.getName())
                    .isPublic(sourceGameList.isPublic())
                    .type(TierListType.FROM_GAMELIST)
                    .sourceGameList(sourceGameList)
                    .build();
            createDefaultSections(tierList);
        }

        synchronizeTierListWithGameList(tierList, sourceGameList);

        TierList refreshedTierList = tierListRepository.findById(tierList.getInternalId())
                .orElseThrow(() -> new IllegalStateException("TierList no encontrada después de creación/sincronización: " + tierList.getPublicId()));

        return tierListMapper.toResponseDTO(refreshedTierList);
    }

    @Transactional
    protected void synchronizeTierListWithGameList(TierList tierList, GameList gameList) {
        if (tierList.getInternalId() != null &&
                (tierList.getType() != TierListType.FROM_GAMELIST ||
                        tierList.getSourceGameList() == null ||
                        !tierList.getSourceGameList().getInternalId().equals(gameList.getInternalId()))) { // Corregido getId a getInternalId para GameList
            logger.warn("Intento de sincronizar TierList ID {} que no es del tipo FROM_GAMELIST o no corresponde a la GameList ID {}", tierList.getPublicId(), gameList.getPublicId());
            return;
        }

        if (tierList.getInternalId() == null) {
            tierListRepository.save(tierList);
        }

        List<TierSection> currentSections = tierSectionRepository.findByTierListOrderBySectionOrderAsc(tierList);
        tierList.setSections(currentSections); // Asegurar que la lista de secciones esté actualizada y gestionada

        Set<UserGame> gamesInGameList = new HashSet<>(gameList.getUserGames());
        Map<Long, TierListItem> itemsInTierListMap = new HashMap<>();

        currentSections.forEach(section ->
                section.getItems().forEach(item -> {
                    if (item.getUserGame() != null) { // Verificar nulidad de UserGame
                        itemsInTierListMap.put(item.getUserGame().getInternalId(), item);
                    } else {
                        logger.warn("TierListItem con ID {} no tiene UserGame asociado en la sección ID {}. Será ignorado.", item.getInternalId(), section.getInternalId());
                    }
                })
        );

        TierSection unclassifiedSection = currentSections.stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección 'Sin Clasificar' no existe en la TierList " + tierList.getPublicId()));

        for (UserGame gameFromGameList : gamesInGameList) {
            if (gameFromGameList == null || gameFromGameList.getInternalId() == null) continue; // Skip si el UserGame o su ID es nulo

            if (!itemsInTierListMap.containsKey(gameFromGameList.getInternalId())) {
                int nextOrder = unclassifiedSection.getItems().stream().mapToInt(TierListItem::getItemOrder).max().orElse(-1) + 1;
                TierListItem newItem = TierListItem.builder()
                        .tierSection(unclassifiedSection)
                        .userGame(gameFromGameList)
                        .itemOrder(nextOrder)
                        .build();
                tierListItemRepository.save(newItem);
                unclassifiedSection.getItems().add(newItem);
                logger.debug("Añadiendo UserGame ID {} a la sección 'Sin Clasificar' de TierList {}", gameFromGameList.getInternalId(), tierList.getPublicId());
            }
        }

        List<TierListItem> itemsToRemove = new ArrayList<>();
        itemsInTierListMap.values().forEach(itemInTierList -> {
            if (itemInTierList.getUserGame() == null || !gamesInGameList.contains(itemInTierList.getUserGame())) {
                itemsToRemove.add(itemInTierList);
            }
        });

        for (TierListItem itemToRemove : itemsToRemove) {
            TierSection parentSection = itemToRemove.getTierSection();
            if (parentSection != null) { // Verificar que la sección padre exista
                parentSection.getItems().remove(itemToRemove);
                reorderItemsInSection(parentSection);
            }
            tierListItemRepository.delete(itemToRemove);
            logger.debug("Eliminando TierListItem ID {} (UserGame ID {}) de TierList {}", itemToRemove.getInternalId(), (itemToRemove.getUserGame() != null ? itemToRemove.getUserGame().getInternalId() : "N/A"), tierList.getPublicId());
        }
        tierListRepository.saveAndFlush(tierList);
    }


    @Override
    @Transactional(readOnly = true)
    public TierListResponseDTO getTierListByPublicId(UUID tierListPublicId, String userEmail) {
        User currentUser = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;
        TierList tierList = findTierListByPublicIdForReadOrThrow(tierListPublicId, currentUser);
        return tierListMapper.toResponseDTO(tierList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierListResponseDTO> getAllProfileTierListsForUser(String userEmail) {
        User owner = getUserByEmailOrThrow(userEmail);
        List<TierList> tierLists = tierListRepository.findAllByOwnerAndType(owner, TierListType.PROFILE_GLOBAL);
        return tierLists.stream()
                .map(tierListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierListResponseDTO> getAllPublicTierLists() {
        List<TierList> tierLists = tierListRepository.findAllByIsPublicTrue();
        return tierLists.stream()
                .map(tierListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TierListResponseDTO updateTierListMetadata(String userEmail, UUID tierListPublicId, TierListUpdateRequestDTO updateRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        tierListMapper.updateFromUpdateRequestDTO(updateRequestDTO, tierList);

        TierList updatedTierList = tierListRepository.save(tierList);
        logger.info("Metadatos de TierList '{}' (ID público: {}) actualizados por el usuario {}", updatedTierList.getName(), tierListPublicId, userEmail);
        return tierListMapper.toResponseDTO(updatedTierList);
    }

    @Override
    @Transactional
    public void deleteTierList(String userEmail, UUID tierListPublicId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        tierListRepository.delete(tierList);
        logger.info("TierList '{}' (ID público: {}) eliminada por el usuario {}", tierList.getName(), tierListPublicId, userEmail);
    }

    @Override
    @Transactional
    public TierListResponseDTO addSectionToTierList(String userEmail, UUID tierListPublicId, TierSectionRequestDTO sectionRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        long currentUserDefinedSections = tierList.getSections().stream().filter(s -> !s.isDefaultUnclassified()).count();
        if (currentUserDefinedSections >= MAX_USER_DEFINED_SECTIONS) {
            throw new InvalidOperationException("No se pueden añadir más secciones. Límite de " + MAX_USER_DEFINED_SECTIONS + " secciones personalizables alcanzado.");
        }

        int nextOrder = tierList.getSections().stream()
                .filter(s -> !s.isDefaultUnclassified())
                .mapToInt(TierSection::getSectionOrder)
                .max()
                .orElse(USER_SECTION_START_ORDER - 1) + 1;

        TierSection newSection = TierSection.builder()
                .name(sectionRequestDTO.getName())
                .sectionOrder(nextOrder)
                .isDefaultUnclassified(false)
                .build();
        tierList.addSection(newSection);

        TierList updatedTierList = tierListRepository.save(tierList);
        logger.info("Sección '{}' añadida a TierList '{}' por el usuario {}", sectionRequestDTO.getName(), tierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(updatedTierList);
    }

    @Override
    @Transactional
    public TierListResponseDTO updateSectionName(String userEmail, UUID tierListPublicId, Long sectionInternalId, TierSectionRequestDTO sectionRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierSection section = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(sectionInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección con ID " + sectionInternalId + " no encontrada en la TierList " + tierListPublicId));

        section.setName(sectionRequestDTO.getName());
        tierSectionRepository.save(section);

        TierList reloadedTierList = tierListRepository.findById(tierList.getInternalId())
                .orElseThrow(() -> new IllegalStateException("TierList no encontrada después de actualizar sección."));
        logger.info("Nombre de sección ID {} actualizado a '{}' en TierList '{}' por el usuario {}", sectionInternalId, sectionRequestDTO.getName(), reloadedTierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(reloadedTierList);
    }

    @Override
    @Transactional
    public TierListResponseDTO removeSectionFromTierList(String userEmail, UUID tierListPublicId, Long sectionInternalId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierSection sectionToRemove = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(sectionInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección con ID " + sectionInternalId + " no encontrada en la TierList " + tierListPublicId));

        if (sectionToRemove.isDefaultUnclassified()) {
            throw new InvalidOperationException("La sección 'Juegos por Clasificar' no puede ser eliminada.");
        }

        long currentUserDefinedSections = tierList.getSections().stream().filter(s -> !s.isDefaultUnclassified()).count();
        if (currentUserDefinedSections <= MIN_USER_DEFINED_SECTIONS) {
            throw new InvalidOperationException("No se puede eliminar la sección. Debe haber al menos " + MIN_USER_DEFINED_SECTIONS + " sección personalizable.");
        }

        TierSection unclassifiedSection = tierList.getSections().stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección 'Sin Clasificar' no existe en la TierList " + tierList.getPublicId()));

        if (!sectionToRemove.getItems().isEmpty()) {
            List<TierListItem> itemsToMove = new ArrayList<>(sectionToRemove.getItems());
            int currentUnclassifiedOrder = unclassifiedSection.getItems().stream().mapToInt(TierListItem::getItemOrder).max().orElse(-1) + 1;
            for (TierListItem item : itemsToMove) {
                item.setTierSection(unclassifiedSection);
                item.setItemOrder(currentUnclassifiedOrder++);
                // No es necesario añadir a unclassifiedSection.getItems() explícitamente si el item se guarda
                // y la relación se actualiza. Pero para consistencia en memoria:
                unclassifiedSection.getItems().add(item);
                tierListItemRepository.save(item);
            }
            sectionToRemove.getItems().clear();
        }

        tierList.removeSection(sectionToRemove);
        reorderSections(tierList);

        TierList updatedTierList = tierListRepository.save(tierList);
        logger.info("Sección '{}' (ID: {}) eliminada de TierList '{}' por el usuario {}", sectionToRemove.getName(), sectionInternalId, updatedTierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(updatedTierList);
    }

    private void reorderSections(TierList tierList) {
        List<TierSection> userSections = tierList.getSections().stream()
                .filter(s -> !s.isDefaultUnclassified())
                .sorted(Comparator.comparingInt(TierSection::getSectionOrder))
                .collect(Collectors.toList());

        for (int i = 0; i < userSections.size(); i++) {
            userSections.get(i).setSectionOrder(USER_SECTION_START_ORDER + i);
        }
    }

    private void reorderItemsInSection(TierSection section) {
        List<TierListItem> items = section.getItems(); // Asumir que esta lista ya está cargada y es la actual
        items.sort(Comparator.comparingInt(TierListItem::getItemOrder)); // Ordenar por si acaso
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setItemOrder(i);
            tierListItemRepository.save(items.get(i)); // Persistir el cambio de orden
        }
    }


    @Override
    @Transactional
    public TierListResponseDTO addItemToTierListSection(String userEmail, UUID tierListPublicId, Long sectionInternalId, TierListItemAddRequestDTO itemAddRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            throw new InvalidOperationException("Los juegos no se pueden añadir directamente a una TierList vinculada a una GameList. Modifica la GameList original.");
        }

        TierSection targetSection = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(sectionInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección con ID " + sectionInternalId + " no encontrada en la TierList " + tierListPublicId));

        if (targetSection.isDefaultUnclassified()){
            throw new InvalidOperationException("Utiliza el endpoint específico para añadir a la sección 'Juegos por Clasificar'.");
        }

        return addItemToSectionLogic(tierList, targetSection, itemAddRequestDTO.getUserGameId(), itemAddRequestDTO.getOrder(), userEmail);
    }

    @Override
    @Transactional
    public TierListResponseDTO addItemToUnclassifiedSection(String userEmail, UUID tierListPublicId, TierListItemAddRequestDTO itemAddRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            // Aunque para FROM_GAMELIST la sincronización lo haría, explícitamente prevenimos la llamada directa.
            throw new InvalidOperationException("Los juegos se sincronizan automáticamente para TierLists de GameList. No se pueden añadir manualmente aquí.");
        }

        TierSection unclassifiedSection = tierList.getSections().stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección 'Sin Clasificar' no existe en la TierList " + tierList.getPublicId()));

        return addItemToSectionLogic(tierList, unclassifiedSection, itemAddRequestDTO.getUserGameId(), itemAddRequestDTO.getOrder(), userEmail);
    }

    private TierListResponseDTO addItemToSectionLogic(TierList tierList, TierSection targetSection, Long userGameId, Integer requestedOrder, String userEmail) {
        UserGame userGame = userGameRepository.findById(userGameId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGame con ID " + userGameId + " no encontrado."));

        // CORREGIDO AQUÍ: usar getId() para User
        if (!userGame.getUser().getId().equals(tierList.getOwner().getId())) {
            throw new UnauthorizedOperationException("El UserGame ID " + userGameId + " no pertenece al propietario de la TierList.");
        }

        Optional<TierListItem> existingItemOpt = tierListItemRepository.findByTierListAndUserGame(tierList, userGame);

        TierListItem itemToProcess;

        if (existingItemOpt.isPresent()) { // El juego ya está en alguna sección de esta TierList
            itemToProcess = existingItemOpt.get();
            TierSection oldSection = itemToProcess.getTierSection();

            if (!oldSection.getInternalId().equals(targetSection.getInternalId())) { // Mover a una sección diferente
                removeItemFromCurrentPositionMemory(oldSection, itemToProcess); // Quita de la lista en memoria y reordena en memoria

                itemToProcess.setTierSection(targetSection);
                insertItemAtOrderMemory(targetSection, itemToProcess, requestedOrder); // Añade a la lista en memoria y reordena en memoria

                tierListItemRepository.save(itemToProcess);
                // Guardar oldSection explícitamente si sus items fueron reordenados y no se guardan por cascada.
                // Por seguridad, la reordenación debe persistirse.
                reorderItemsInSection(oldSection); // Esto también guarda cada item de oldSection
                logger.info("UserGame ID {} movido a la sección '{}' en TierList '{}'", userGameId, targetSection.getName(), tierList.getName());
            } else { // Ya está en la misma sección, solo posible reorden
                if (requestedOrder != null && itemToProcess.getItemOrder() != requestedOrder) {
                    removeItemFromCurrentPositionMemory(targetSection, itemToProcess);
                    insertItemAtOrderMemory(targetSection, itemToProcess, requestedOrder);
                    tierListItemRepository.save(itemToProcess);
                    logger.info("UserGame ID {} reordenado en la sección '{}' en TierList '{}'", userGameId, targetSection.getName(), tierList.getName());
                } else {
                    logger.warn("UserGame ID {} ya está en la sección '{}' y en la posición deseada (o no se especificó orden).", userGameId, targetSection.getName());
                }
            }
        } else { // Añadir nuevo item
            itemToProcess = TierListItem.builder()
                    .userGame(userGame)
                    .tierSection(targetSection) // Asignar la sección
                    .itemOrder(0) // Orden temporal, se ajustará
                    .build();
            // targetSection.addItem(itemToProcess); // No es necesario si insertItemAtOrderMemory lo hace y luego se guarda el item
            insertItemAtOrderMemory(targetSection, itemToProcess, requestedOrder);
            tierListItemRepository.save(itemToProcess);
            logger.info("UserGame ID {} añadido a la sección '{}' en TierList '{}'", userGameId, targetSection.getName(), tierList.getName());
        }

        // Guardar la sección destino para persistir la lista de items actualizada
        tierSectionRepository.save(targetSection);
        TierList updatedTierList = tierListRepository.findById(tierList.getInternalId()).orElseThrow();
        return tierListMapper.toResponseDTO(updatedTierList);
    }

    // Ajusta órdenes en MEMORIA y añade/mueve el item en la lista de la sección.
    private void insertItemAtOrderMemory(TierSection section, TierListItem itemToAddOrMove, Integer requestedOrder) {
        List<TierListItem> items = section.getItems();

        // Si el item ya está en la lista (porque se está moviendo dentro de la misma sección), quitarlo primero.
        items.remove(itemToAddOrMove);

        int targetIdx;
        if (requestedOrder == null || requestedOrder < 0 || requestedOrder > items.size()) {
            targetIdx = items.size(); // Añadir al final
        } else {
            targetIdx = requestedOrder;
        }

        // Desplazar items existentes que están desde targetIdx en adelante
        for (int i = targetIdx; i < items.size(); i++) {
            items.get(i).setItemOrder(items.get(i).getItemOrder() + 1);
        }

        items.add(targetIdx, itemToAddOrMove); // Añadir/reinsertar en la posición correcta
        itemToAddOrMove.setItemOrder(targetIdx); // Establecer el orden del item

        // Re-numerar todos los items de la sección para asegurar consistencia después de la inserción/movimiento
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setItemOrder(i);
        }
    }

    // Quita de la lista en MEMORIA y ajusta órdenes en MEMORIA.
    private void removeItemFromCurrentPositionMemory(TierSection section, TierListItem itemToRemove) {
        int oldOrder = itemToRemove.getItemOrder();
        if(section.getItems().remove(itemToRemove)){ // Solo si se removió exitosamente
            // Re-numerar los items restantes en la sección
            for (TierListItem item : section.getItems()) {
                if (item.getItemOrder() > oldOrder) {
                    item.setItemOrder(item.getItemOrder() - 1);
                }
            }
        }
    }


    @Override
    @Transactional
    public TierListResponseDTO moveItemInTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId, TierListItemMoveRequestDTO itemMoveRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierListItem itemToMove = tierListItemRepository.findByInternalIdAndTierListPublicId(tierListItemInternalId, tierListPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("TierListItem con ID " + tierListItemInternalId + " no encontrado en la TierList " + tierListPublicId));

        TierSection oldSection = itemToMove.getTierSection();
        TierSection newSection = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(itemMoveRequestDTO.getTargetSectionInternalId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección destino con ID " + itemMoveRequestDTO.getTargetSectionInternalId() + " no encontrada."));

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            GameList sourceGameList = tierList.getSourceGameList();
            if (sourceGameList == null || itemToMove.getUserGame() == null || !sourceGameList.getUserGames().contains(itemToMove.getUserGame())) {
                throw new InvalidOperationException("El juego asociado a este item ya no pertenece a la GameList origen o hay una inconsistencia.");
            }
        }

        removeItemFromCurrentPositionMemory(oldSection, itemToMove); // Quita de la lista de oldSection y reordena en memoria
        itemToMove.setTierSection(newSection); // Cambia la asociación
        insertItemAtOrderMemory(newSection, itemToMove, itemMoveRequestDTO.getNewOrder()); // Añade a newSection y reordena en memoria

        tierListItemRepository.save(itemToMove);

        if (!oldSection.getInternalId().equals(newSection.getInternalId())) {
            reorderItemsInSection(oldSection); // Persiste reorden de oldSection
        }
        reorderItemsInSection(newSection); // Persiste reorden de newSection (incluyendo el item movido)

        TierList updatedTierList = tierListRepository.findById(tierList.getInternalId()).orElseThrow();
        logger.info("Item ID {} movido a sección '{}' (orden {}) en TierList '{}' por usuario {}", tierListItemInternalId, newSection.getName(), itemToMove.getItemOrder(), tierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(updatedTierList);
    }


    @Override
    @Transactional
    public TierListResponseDTO removeItemFromTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierListItem itemToRemove = tierListItemRepository.findByInternalIdAndTierListPublicId(tierListItemInternalId, tierListPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("TierListItem con ID " + tierListItemInternalId + " no encontrado en la TierList " + tierListPublicId));

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            throw new InvalidOperationException("Los juegos no se pueden eliminar directamente de una TierList vinculada a una GameList. Modifica la GameList original.");
        }

        TierSection section = itemToRemove.getTierSection();
        removeItemFromCurrentPositionMemory(section, itemToRemove);
        tierListItemRepository.delete(itemToRemove);

        reorderItemsInSection(section); // Persiste el reordenamiento de la sección afectada

        TierList updatedTierList = tierListRepository.findById(tierList.getInternalId()).orElseThrow();
        logger.info("Item ID {} (UserGame ID: {}) eliminado de TierList '{}' por usuario {}",
                itemToRemove.getInternalId(), (itemToRemove.getUserGame() != null ? itemToRemove.getUserGame().getInternalId() : "N/A"), tierList.getName(), userEmail);
        return tierListMapper.toResponseDTO(updatedTierList);
    }
}