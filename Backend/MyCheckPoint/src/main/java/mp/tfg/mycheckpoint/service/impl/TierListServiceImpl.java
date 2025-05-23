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
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager; // Importar EntityManager
import jakarta.persistence.PersistenceContext; // Importar PersistenceContext


import java.util.*;
import java.util.stream.Collectors;

@Service
public class TierListServiceImpl implements TierListService {

    private static final Logger logger = LoggerFactory.getLogger(TierListServiceImpl.class);
    // ... (constantes sin cambios) ...
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

    @PersistenceContext // Inyectar EntityManager
    private EntityManager entityManager;

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

    // ... (métodos getUserByEmailOrThrow, initializeTierListDetails, findTierListByPublicIdAndOwnerOrThrow, findTierListByPublicIdForReadOrThrow sin cambios) ...
    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private void initializeTierListDetails(TierList tierList) {
        if (tierList == null) return;
        if (tierList.getSections() != null) {
            for (TierSection section : tierList.getSections()) {
                Hibernate.initialize(section.getItems());
                if (section.getItems() != null) {
                    for (TierListItem item : section.getItems()) {
                        if (item.getUserGame() != null) {
                            Hibernate.initialize(item.getUserGame());
                            if (item.getUserGame().getGame() != null) {
                                Hibernate.initialize(item.getUserGame().getGame());
                                if (item.getUserGame().getGame().getCover() != null) {
                                    Hibernate.initialize(item.getUserGame().getGame().getCover());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private TierList findTierListByPublicIdAndOwnerOrThrow(UUID publicId, User owner) {
        Optional<TierList> tierListOpt = tierListRepository.findByPublicIdAndOwnerWithSections(publicId, owner);
        tierListOpt.ifPresent(this::initializeTierListDetails);
        return tierListOpt.orElseThrow(() -> new ResourceNotFoundException("TierList con ID público " + publicId + " no encontrada para el usuario " + owner.getNombreUsuario()));
    }

    private TierList findTierListByPublicIdForReadOrThrow(UUID publicId, User currentUser) {
        TierList tierListBasic = tierListRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("TierList no encontrada con ID público: " + publicId));

        if (!tierListBasic.isPublic() && (currentUser == null || !tierListBasic.getOwner().getId().equals(currentUser.getId()))) {
            throw new UnauthorizedOperationException("No tienes permiso para acceder a esta TierList.");
        }

        Optional<TierList> tierListOpt;
        if (tierListBasic.isPublic()) {
            tierListOpt = tierListRepository.findByPublicIdAndIsPublicTrueWithSections(publicId);
        } else {
            tierListOpt = tierListRepository.findByPublicIdAndOwnerWithSections(publicId, currentUser);
        }

        tierListOpt.ifPresent(this::initializeTierListDetails);
        return tierListOpt.orElseThrow(() -> new ResourceNotFoundException("TierList no encontrada o inaccesible con ID: " + publicId));
    }


    private void moveItemsToUnclassifiedSectionAndPersist(TierList tierList, TierSection sourceSection, TierSection unclassifiedSection) {
        if (sourceSection.getItems().isEmpty()) {
            logger.debug("No hay ítems que mover de la sección '{}' (ID: {})", sourceSection.getName(), sourceSection.getInternalId());
            return;
        }

        List<TierListItem> itemsToMove = new ArrayList<>(sourceSection.getItems());
        sourceSection.getItems().clear(); // Desvincular de la colección de la sección origen en memoria

        int currentOrderInUnclassified = unclassifiedSection.getItems().size();

        for (TierListItem item : itemsToMove) {
            item.setTierSection(unclassifiedSection); // Actualizar la referencia en el ítem
            item.setItemOrder(currentOrderInUnclassified++);

            // Como 'item' es una entidad gestionada que se ha vuelto "sucia" (dirty),
            // y 'unclassifiedSection' también es gestionada, añadir 'item' a la colección
            // de 'unclassifiedSection' debería ser suficiente para que Hibernate rastree el cambio.
            // El merge explícito aquí podría ser redundante si el estado ya es gestionado y sucio,
            // pero se mantiene por robustez si el estado de 'item' fuera incierto.
            TierListItem managedItem = entityManager.merge(item); // Asegurar que el item está gestionado y los cambios registrados
            unclassifiedSection.getItems().add(managedItem); // Añadir la instancia gestionada a la colección
        }

        // Reordenar ítems en la sección "Sin Clasificar" si es necesario
        reorderItemsAndUpdate(unclassifiedSection.getItems());

        // Guardar la sección "Sin Clasificar" para persistir los ítems movidos y sus nuevos estados.
        // Esto hace un flush de los cambios en los TierListItem y en la colección de unclassifiedSection.
        tierSectionRepository.saveAndFlush(unclassifiedSection);
        logger.debug("Ítems de la sección ID {} movidos y persistidos en la sección 'Sin Clasificar' ID {}", sourceSection.getInternalId(), unclassifiedSection.getInternalId());
    }


    @Override
    @Transactional
    public TierListResponseDTO removeSectionFromTierList(String userEmail, UUID tierListPublicId, Long sectionInternalId) {
        User owner = getUserByEmailOrThrow(userEmail);
        // Cargar TierList y sus secciones. Los ítems se cargarán al acceder o por initializeTierListDetails.
        TierList tierList = tierListRepository.findByPublicIdAndOwnerWithSections(tierListPublicId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("TierList con ID público " + tierListPublicId + " no encontrada para el usuario " + owner.getNombreUsuario()));

        // Es importante que todas las entidades estén completamente inicializadas y gestionadas.
        initializeTierListDetails(tierList);

        TierSection sectionToRemove = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(sectionInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección con ID " + sectionInternalId + " no encontrada en la TierList " + tierListPublicId));

        if (sectionToRemove.isDefaultUnclassified()) {
            throw new InvalidOperationException("La sección 'Juegos por Clasificar' no puede ser eliminada.");
        }
        if (tierList.getSections().stream().filter(s -> !s.isDefaultUnclassified()).count() <= MIN_USER_DEFINED_SECTIONS) {
            throw new InvalidOperationException("No se puede eliminar la sección. Debe haber al menos " + MIN_USER_DEFINED_SECTIONS + " sección personalizable.");
        }

        TierSection unclassifiedSection = tierList.getSections().stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección 'Sin Clasificar' no existe en la TierList " + tierListPublicId));

        if (!sectionToRemove.getItems().isEmpty()) {
            List<TierListItem> itemsToMove = new ArrayList<>(sectionToRemove.getItems()); // Copia para iterar

            // 1. Actualizar la FK de cada TierListItem y guardarlo.
            for (TierListItem item : itemsToMove) {
                item.setTierSection(unclassifiedSection);
                // El orden se establecerá globalmente más tarde para unclassifiedSection.
                // No se manipulan las colecciones aquí todavía, solo la referencia en el item.
                tierListItemRepository.save(item); // Persiste el cambio de FK en 'item'.
            }

            // 2. Forzar el flush para que los UPDATEs de los TierListItem se envíen a la BD.
            // Ahora, en la BD, los ítems ya pertenecen a 'unclassifiedSection'.
            entityManager.flush();

            // 3. Refrescar las secciones para que sus colecciones 'items' reflejen el estado de la BD.
            // Esto es crucial para que las colecciones en memoria estén sincronizadas con los cambios de FK.
            entityManager.refresh(unclassifiedSection);
            entityManager.refresh(sectionToRemove);
            // Después de refrescar, unclassifiedSection.getItems() debería contener los ítems movidos,
            // y sectionToRemove.getItems() debería estar vacía (o no contener los ítems movidos).

            // 4. Reordenar los ítems en unclassifiedSection (que ahora tiene los ítems movidos)
            reorderItemsAndUpdate(unclassifiedSection.getItems());
            tierSectionRepository.save(unclassifiedSection); // Guardar los cambios de orden
        }

        // 5. Eliminar 'sectionToRemove' de la colección de 'tierList'.
        // orphanRemoval en TierList.sections la eliminará de la BD.
        // orphanRemoval en TierSection.items (para 'sectionToRemove') actuará sobre su colección 'items'.
        // Como los ítems ya fueron reasignados en BD y la colección de sectionToRemove debería estar
        // vacía o no contenerlos tras el refresh, no debería haber conflicto.
        tierList.getSections().remove(sectionToRemove);
        reorderSections(tierList);

        TierList updatedTierList = tierListRepository.save(tierList);

        logger.info("Sección '{}' (ID: {}) eliminada de TierList. Ítems movidos (si los había). Usuario: {}",
                sectionToRemove.getName(), sectionInternalId, userEmail);

        TierList reloadedTierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);
        return tierListMapper.toResponseDTO(reloadedTierList);
    }

    // ... (el resto de los métodos de la clase: createProfileTierList, getOrCreateTierListForGameList,
    //      synchronizeTierListWithGameList, getTierListByPublicId, getAllProfileTierListsForUser,
    //      getAllPublicTierLists, updateTierListMetadata, deleteTierList, addSectionToTierList,
    //      updateSectionName, reorderItemsAndUpdate, reorderSections, addItemToTierListSection,
    //      addItemToUnclassifiedSection, addItemToSectionLogic, moveItemInTierList, removeItemFromTierList
    //      deben estar aquí, tal como los tenías o con las correcciones previas)

    // Asegúrate de incluir los métodos de ayuda que ya tenías:
    private void reorderItemsAndUpdate(List<TierListItem> items) {
        for (int i = 0; i < items.size(); i++) {
            TierListItem currentItem = items.get(i);
            if (currentItem.getItemOrder() != i) {
                currentItem.setItemOrder(i);
            }
        }
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
    // ... (y cualquier otro método de la clase) ...
    // COPIA Y PEGA EL RESTO DE TUS MÉTODOS DE TierListServiceImpl AQUÍ
    // (los que no te he mostrado para modificar)
    // Es importante que tengas la clase completa. Solo he modificado removeSectionFromTierList
    // y he añadido la inyección de EntityManager.

    // ... (métodos createProfileTierList, getOrCreateTierListForGameList, synchronizeTierListWithGameList, etc., como estaban antes o con correcciones previas)
    // Debes pegar aquí el resto de los métodos de TierListServiceImpl
    // que no se han modificado en esta respuesta específica.
    // Solo he mostrado removeSectionFromTierList, reorderItemsAndUpdate, reorderSections, y los métodos de carga/inicialización.
    // Asegúrate de tener todos los demás métodos (addItemToSection, moveItem, etc.) en tu clase.
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

        initializeTierListDetails(savedTierList);
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

        Hibernate.initialize(sourceGameList.getUserGames());

        if (!sourceGameList.getOwner().getId().equals(currentUser.getId()) && !sourceGameList.isPublic()) {
            throw new UnauthorizedOperationException("No tienes permiso para acceder a la GameList asociada.");
        }

        Optional<TierList> existingTierListOpt = tierListRepository.findBySourceGameListAndType(sourceGameList, TierListType.FROM_GAMELIST);

        TierList tierList;
        if (existingTierListOpt.isPresent()) {
            tierList = existingTierListOpt.get();
            initializeTierListDetails(tierList);
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
            tierList = tierListRepository.save(tierList);
            initializeTierListDetails(tierList);
        }

        synchronizeTierListWithGameList(tierList, sourceGameList);

        TierList finalTierListState = findTierListByPublicIdForReadOrThrow(tierList.getPublicId(), tierList.getOwner());
        return tierListMapper.toResponseDTO(finalTierListState);
    }

    @Transactional
    protected void synchronizeTierListWithGameList(TierList tierList, GameList gameList) {
        if (tierList.getType() != TierListType.FROM_GAMELIST || tierList.getSourceGameList() == null || !tierList.getSourceGameList().getInternalId().equals(gameList.getInternalId())) {
            logger.warn("Intento de sincronizar TierList ID {} que no es del tipo FROM_GAMELIST o no corresponde a la GameList ID {}", tierList.getPublicId(), gameList.getPublicId());
            return;
        }
        Hibernate.initialize(tierList.getSections());
        tierList.getSections().forEach(section -> Hibernate.initialize(section.getItems()));
        Hibernate.initialize(gameList.getUserGames());

        Set<UserGame> gamesInGameList = new HashSet<>(gameList.getUserGames());
        Map<Long, TierListItem> itemsInTierListMap = new HashMap<>();

        tierList.getSections().forEach(section ->
                section.getItems().forEach(item -> {
                    if (item.getUserGame() != null) {
                        itemsInTierListMap.put(item.getUserGame().getInternalId(), item);
                    }
                })
        );

        TierSection unclassifiedSection = tierList.getSections().stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección 'Sin Clasificar' no existe en la TierList " + tierList.getPublicId()));

        boolean changesMade = false;

        for (UserGame gameFromGameList : gamesInGameList) {
            if (gameFromGameList == null || gameFromGameList.getInternalId() == null) continue;

            if (!itemsInTierListMap.containsKey(gameFromGameList.getInternalId())) {
                TierListItem newItem = TierListItem.builder()
                        .tierSection(unclassifiedSection)
                        .userGame(gameFromGameList)
                        .itemOrder(unclassifiedSection.getItems().size())
                        .build();
                unclassifiedSection.getItems().add(newItem);
                changesMade = true;
                logger.debug("Añadiendo UserGame ID {} a la sección 'Sin Clasificar' de TierList {} durante la sincronización", gameFromGameList.getInternalId(), tierList.getPublicId());
            }
        }

        List<TierListItem> itemsToRemoveFromTierList = new ArrayList<>();
        for (TierListItem itemInTierList : itemsInTierListMap.values()) {
            if (itemInTierList.getUserGame() == null || !gamesInGameList.contains(itemInTierList.getUserGame())) {
                itemsToRemoveFromTierList.add(itemInTierList);
            }
        }

        if (!itemsToRemoveFromTierList.isEmpty()) {
            changesMade = true;
            for (TierListItem itemToRemove : itemsToRemoveFromTierList) {
                TierSection parentSection = itemToRemove.getTierSection();
                if (parentSection != null) {
                    parentSection.getItems().remove(itemToRemove);
                }
                logger.debug("Eliminando TierListItem ID {} (UserGame ID {}) de TierList {} durante la sincronización",
                        itemToRemove.getInternalId(),
                        (itemToRemove.getUserGame() != null ? itemToRemove.getUserGame().getInternalId() : "N/A"),
                        tierList.getPublicId());
            }
        }

        if (changesMade) {
            tierList.getSections().forEach(section -> this.reorderItemsAndUpdate(section.getItems()));
            tierListRepository.saveAndFlush(tierList);
            logger.info("TierList {} sincronizada con GameList {}.", tierList.getPublicId(), gameList.getPublicId());
        } else {
            logger.debug("No se realizaron cambios en la TierList {} durante la sincronización con GameList {}.", tierList.getPublicId(), gameList.getPublicId());
        }
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
        tierLists.forEach(this::initializeTierListDetails);
        return tierLists.stream()
                .map(tierListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierListResponseDTO> getAllPublicTierLists() {
        List<TierList> tierLists = tierListRepository.findAllByIsPublicTrue();
        tierLists.forEach(this::initializeTierListDetails);
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

        initializeTierListDetails(updatedTierList);
        return tierListMapper.toResponseDTO(updatedTierList);
    }

    @Override
    @Transactional
    public void deleteTierList(String userEmail, UUID tierListPublicId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner); // Carga con detalles

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

        initializeTierListDetails(updatedTierList);
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

        TierList updatedTierList = tierListRepository.save(tierList);
        logger.info("Nombre de sección ID {} actualizado a '{}' en TierList '{}' por el usuario {}", sectionInternalId, sectionRequestDTO.getName(), updatedTierList.getName(), userEmail);

        initializeTierListDetails(updatedTierList);
        return tierListMapper.toResponseDTO(updatedTierList);
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

        if (!userGame.getUser().getId().equals(tierList.getOwner().getId())) {
            throw new UnauthorizedOperationException("El UserGame ID " + userGameId + " no pertenece al propietario de la TierList.");
        }

        Optional<TierListItem> existingItemOpt = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> item.getUserGame().getInternalId().equals(userGameId))
                .findFirst();

        TierListItem itemToProcess;

        if (existingItemOpt.isPresent()) {
            itemToProcess = existingItemOpt.get();
            TierSection oldSection = itemToProcess.getTierSection();

            if (!oldSection.getInternalId().equals(targetSection.getInternalId())) {
                oldSection.getItems().removeIf(i -> i.getInternalId().equals(itemToProcess.getInternalId()));
                reorderItemsAndUpdate(oldSection.getItems());
                itemToProcess.setTierSection(targetSection);
            }
        } else {
            itemToProcess = TierListItem.builder()
                    .userGame(userGame)
                    .tierSection(targetSection)
                    .build();
        }

        List<TierListItem> targetItems = targetSection.getItems();
        targetItems.removeIf(i -> i.getInternalId() != null && i.getInternalId().equals(itemToProcess.getInternalId()));

        int targetIdx;
        if (requestedOrder == null || requestedOrder < 0 || requestedOrder > targetItems.size()) {
            targetIdx = targetItems.size();
        } else {
            targetIdx = requestedOrder;
        }
        targetItems.add(targetIdx, itemToProcess);
        reorderItemsAndUpdate(targetItems);

        TierList updatedTierList = tierListRepository.save(tierList);
        logger.info("Item (UserGame ID: {}) {} sección '{}' en TierList '{}'. Usuario: {}",
                userGameId, existingItemOpt.isPresent() ? "movido/reordenado en" : "añadido a",
                targetSection.getName(), tierList.getName(), userEmail);

        initializeTierListDetails(updatedTierList);
        return tierListMapper.toResponseDTO(updatedTierList);
    }


    @Override
    @Transactional
    public TierListResponseDTO moveItemInTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId, TierListItemMoveRequestDTO itemMoveRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierListItem itemToMove = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> item.getInternalId().equals(tierListItemInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("TierListItem con ID " + tierListItemInternalId + " no encontrado en la TierList " + tierListPublicId));

        TierSection originalOldSectionRef = itemToMove.getTierSection();
        if (originalOldSectionRef == null) {
            throw new IllegalStateException("El ítem a mover (ID: " + itemToMove.getInternalId() + ") no tiene una sección antigua asociada.");
        }
        final Long oldSectionIdToFilterBy = originalOldSectionRef.getInternalId();

        TierSection oldSection = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(oldSectionIdToFilterBy))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sección antigua (ID: " + oldSectionIdToFilterBy + ") del ítem no se encontró en la TierList cargada."));

        TierSection newSection = tierList.getSections().stream()
                .filter(s -> s.getInternalId().equals(itemMoveRequestDTO.getTargetSectionInternalId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección destino con ID " + itemMoveRequestDTO.getTargetSectionInternalId() + " no encontrada."));

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            GameList sourceGameList = tierList.getSourceGameList();
            if (sourceGameList != null) Hibernate.initialize(sourceGameList.getUserGames());

            if (sourceGameList == null || itemToMove.getUserGame() == null || !sourceGameList.getUserGames().contains(itemToMove.getUserGame())) {
                throw new InvalidOperationException("El juego asociado a este item ya no pertenece a la GameList origen o hay una inconsistencia.");
            }
        }

        if (!oldSection.getInternalId().equals(newSection.getInternalId())) {
            oldSection.getItems().removeIf(item -> item.getInternalId().equals(itemToMove.getInternalId()));
            reorderItemsAndUpdate(oldSection.getItems());
        }

        itemToMove.setTierSection(newSection);

        List<TierListItem> newSectionItems = newSection.getItems();
        newSectionItems.removeIf(item -> item.getInternalId().equals(itemToMove.getInternalId()));

        int targetOrderInNewSection = itemMoveRequestDTO.getNewOrder();
        if (targetOrderInNewSection < 0 || targetOrderInNewSection > newSectionItems.size()) {
            newSectionItems.add(itemToMove);
        } else {
            newSectionItems.add(targetOrderInNewSection, itemToMove);
        }
        reorderItemsAndUpdate(newSectionItems);

        TierList updatedTierList = tierListRepository.saveAndFlush(tierList);

        logger.info("Item ID {} (UserGame ID: {}) movido de sección ID {} a sección ID {} (orden final: {}) en TierList '{}' por usuario {}",
                itemToMove.getInternalId(),
                (itemToMove.getUserGame() != null ? itemToMove.getUserGame().getInternalId() : "N/A"),
                oldSection.getInternalId(),
                newSection.getInternalId(),
                itemToMove.getItemOrder(),
                tierList.getName(), userEmail);

        initializeTierListDetails(updatedTierList);
        return tierListMapper.toResponseDTO(updatedTierList);
    }


    @Override
    @Transactional
    public TierListResponseDTO removeItemFromTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierListItem itemToRemove = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> item.getInternalId().equals(tierListItemInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("TierListItem con ID " + tierListItemInternalId + " no encontrado en la TierList " + tierListPublicId));

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            throw new InvalidOperationException("Los juegos no se pueden eliminar directamente de una TierList vinculada a una GameList. Modifica la GameList original.");
        }

        TierSection section = itemToRemove.getTierSection();
        if (section != null) {
            section.getItems().removeIf(i -> i.getInternalId().equals(itemToRemove.getInternalId()));
            reorderItemsAndUpdate(section.getItems());
        } else {
            logger.warn("El ítem ID {} a eliminar no tenía una sección asociada.", tierListItemInternalId);
        }

        TierList updatedTierList = tierListRepository.saveAndFlush(tierList);
        logger.info("Item ID {} (UserGame ID: {}) eliminado de TierList '{}' por usuario {}",
                itemToRemove.getInternalId(), (itemToRemove.getUserGame() != null ? itemToRemove.getUserGame().getInternalId() : "N/A"), tierList.getName(), userEmail);

        TierList reloadedTierList = findTierListByPublicIdAndOwnerOrThrow(updatedTierList.getPublicId(), owner);
        return tierListMapper.toResponseDTO(reloadedTierList);
    }
}