// Archivo: MyCheckPoint/src/main/java/mp/tfg/mycheckpoint/service/impl/TierListServiceImpl.java
package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.enums.TierListType;
import mp.tfg.mycheckpoint.dto.tierlist.*;
import mp.tfg.mycheckpoint.entity.*;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

    @PersistenceContext
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

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private void initializeTierListDetails(TierList tierList) {
        if (tierList == null) return;
        Hibernate.initialize(tierList.getSections());
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

        if (!tierListBasic.isPublic() && (currentUser == null || !Objects.equals(tierListBasic.getOwner().getId(), currentUser.getId()))) {
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

    private void reorderItemsAndUpdate(List<TierListItem> items) {
        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            TierListItem currentItem = items.get(i);
            if (currentItem.getItemOrder() != i) {
                currentItem.setItemOrder(i);
            }
        }
    }

    private void reorderSections(TierList tierList) {
        if (tierList == null || tierList.getSections() == null) return;
        List<TierSection> userSections = tierList.getSections().stream()
                .filter(s -> !s.isDefaultUnclassified())
                .sorted(Comparator.comparingInt(TierSection::getSectionOrder))
                .collect(Collectors.toList());

        for (int i = 0; i < userSections.size(); i++) {
            userSections.get(i).setSectionOrder(USER_SECTION_START_ORDER + i);
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
        initializeTierListDetails(savedTierList);
        logger.info("TierList de perfil '{}' creada para el usuario {}", savedTierList.getName(), userEmail);
        return tierListMapper.toTierListResponseDTOWithSections(savedTierList);
    }

    private void createDefaultSections(TierList tierList) {
        TierSection unclassifiedSection = TierSection.builder()
                .tierList(tierList)
                .name(UNCLASSIFIED_SECTION_NAME)
                .sectionOrder(UNCLASSIFIED_SECTION_ORDER)
                .isDefaultUnclassified(true)
                .items(new ArrayList<>())
                .build();
        tierList.addSection(unclassifiedSection);

        String[] defaultNames = {"S", "A", "B", "C", "D"};
        for (int i = 0; i < defaultNames.length; i++) {
            TierSection section = TierSection.builder()
                    .tierList(tierList)
                    .name(defaultNames[i])
                    .sectionOrder(USER_SECTION_START_ORDER + i)
                    .isDefaultUnclassified(false)
                    .items(new ArrayList<>())
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

        // Cargar explícitamente los userGames si la primera carga no lo hizo (findByPublicId no tiene fetch)
        Hibernate.initialize(sourceGameList.getUserGames());
        Hibernate.initialize(sourceGameList.getOwner());


        if (!sourceGameList.isPublic() && !Objects.equals(sourceGameList.getOwner().getId(), currentUser.getId())) {
            throw new UnauthorizedOperationException("No tienes permiso para acceder a la GameList asociada.");
        }
        User ownerOfTierList = sourceGameList.getOwner();

        Optional<TierList> existingTierListOpt = tierListRepository.findBySourceGameListAndType(sourceGameList, TierListType.FROM_GAMELIST);
        TierList tierList;
        if (existingTierListOpt.isPresent()) {
            tierList = existingTierListOpt.get();
            Hibernate.initialize(tierList.getSections());
            tierList.getSections().forEach(s -> Hibernate.initialize(s.getItems()));
            logger.debug("TierList existente encontrada para GameList {}. Sincronizando juegos...", gameListPublicId);
        } else {
            logger.info("Creando nueva TierList para GameList {} ({})", sourceGameList.getName(), gameListPublicId);
            tierList = TierList.builder()
                    .owner(ownerOfTierList)
                    .name(sourceGameList.getName())
                    .description("Tier list para la lista de juegos: " + sourceGameList.getName())
                    .isPublic(sourceGameList.isPublic())
                    .type(TierListType.FROM_GAMELIST)
                    .sourceGameList(sourceGameList)
                    .build();
            createDefaultSections(tierList);
            tierList = tierListRepository.save(tierList);
        }

        synchronizeTierListWithGameList(tierList, sourceGameList);

        TierList finalTierListState = findTierListByPublicIdForReadOrThrow(tierList.getPublicId(), currentUser); // currentUser para validación de lectura
        return tierListMapper.toTierListResponseDTOWithSections(finalTierListState);
    }

    @Transactional
    protected void synchronizeTierListWithGameList(TierList tierList, GameList gameList) {
        if (tierList.getType() != TierListType.FROM_GAMELIST || tierList.getSourceGameList() == null || !Objects.equals(tierList.getSourceGameList().getInternalId(), gameList.getInternalId())) {
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
        Hibernate.initialize(unclassifiedSection.getItems());


        boolean changesMade = false;
        for (UserGame gameFromGameList : gamesInGameList) {
            if (gameFromGameList == null || gameFromGameList.getInternalId() == null) continue;
            if (!itemsInTierListMap.containsKey(gameFromGameList.getInternalId())) {
                TierListItem newItem = TierListItem.builder()
                        .tierSection(unclassifiedSection)
                        .userGame(gameFromGameList)
                        .itemOrder(unclassifiedSection.getItems().size())
                        .build();
                unclassifiedSection.addItem(newItem);
                changesMade = true;
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
                    parentSection.removeItem(itemToRemove);
                }
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
        return tierListMapper.toTierListResponseDTOWithSections(tierList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierListResponseDTO> getAllProfileTierListsForUser(String userEmail) {
        User owner = getUserByEmailOrThrow(userEmail);
        List<TierList> tierLists = tierListRepository.findAllByOwnerAndTypeWithSections(owner);
        tierLists.forEach(this::initializeTierListDetails);
        return tierLists.stream()
                .map(tierListMapper::toTierListResponseDTOWithSections)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierListResponseDTO> getAllPublicTierLists() {
        List<TierList> tierLists = tierListRepository.findAllByIsPublicTrueAndFetchSections(); // Usando el nuevo método del repo
        tierLists.forEach(this::initializeTierListDetails);
        return tierLists.stream()
                .map(tierListMapper::toTierListResponseDTOWithSections) // Usando el método correcto del mapper
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TierListResponseDTO updateTierListMetadata(String userEmail, UUID tierListPublicId, TierListUpdateRequestDTO updateRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);
        tierListMapper.updateFromUpdateRequestDTO(updateRequestDTO, tierList);
        TierList updatedTierList = tierListRepository.save(tierList);
        initializeTierListDetails(updatedTierList);
        return tierListMapper.toTierListResponseDTOWithSections(updatedTierList);
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
                .items(new ArrayList<>())
                .build();
        tierList.addSection(newSection);
        TierList updatedTierList = tierListRepository.save(tierList);
        initializeTierListDetails(updatedTierList);
        return tierListMapper.toTierListResponseDTOWithSections(updatedTierList);
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
        return tierListMapper.toTierListResponseDTOWithSections(reloadedTierList);
    }

    @Override // Asegúrate de que esta anotación esté presente
    @Transactional
    public TierListResponseDTO updateSectionName(String userEmail, UUID tierListPublicId, Long sectionInternalId, TierSectionRequestDTO sectionRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);
        TierSection section = tierList.getSections().stream()
                .filter(s -> Objects.equals(s.getInternalId(), sectionInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección con ID " + sectionInternalId + " no encontrada en la TierList " + tierListPublicId));

        // Actualizar el nombre de la sección
        section.setName(sectionRequestDTO.getName());

        // Guardar los cambios en la TierList (la sección es parte de TierList y se guardará por cascada)
        TierList updatedTierList = tierListRepository.save(tierList); // O saveAndFlush si prefieres

        // Es buena práctica inicializar los detalles para el DTO de respuesta
        initializeTierListDetails(updatedTierList);

        return tierListMapper.toTierListResponseDTOWithSections(updatedTierList);
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
                .filter(s -> Objects.equals(s.getInternalId(),sectionInternalId))
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
        if (!Objects.equals(userGame.getUser().getId(), tierList.getOwner().getId())) {
            throw new UnauthorizedOperationException("El UserGame ID " + userGameId + " no pertenece al propietario de la TierList.");
        }

        Hibernate.initialize(targetSection.getItems()); // Cargar items de la sección destino
        // Cargar items de todas las secciones para buscar duplicados y para mover si es necesario
        tierList.getSections().forEach(s -> Hibernate.initialize(s.getItems()));


        Optional<TierListItem> existingItemAnywhereOpt = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> Objects.equals(item.getUserGame().getInternalId(), userGameId))
                .findFirst();

        TierListItem itemToProcess;
        if (existingItemAnywhereOpt.isPresent()) {
            itemToProcess = existingItemAnywhereOpt.get();
            TierSection oldSection = itemToProcess.getTierSection();
            if (!Objects.equals(oldSection.getInternalId(), targetSection.getInternalId())) {
                oldSection.getItems().removeIf(i -> i.getInternalId() != null && Objects.equals(i.getInternalId(), itemToProcess.getInternalId()));
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
        targetItems.removeIf(i -> i.getInternalId() != null && Objects.equals(i.getInternalId(), itemToProcess.getInternalId()));

        int targetIdx = (requestedOrder == null || requestedOrder < 0 || requestedOrder > targetItems.size()) ? targetItems.size() : requestedOrder;
        targetItems.add(targetIdx, itemToProcess);
        reorderItemsAndUpdate(targetItems);

        TierList updatedTierList = tierListRepository.saveAndFlush(tierList);
        logger.info("Item (UserGame ID: {}) {} sección '{}' en TierList '{}'. Usuario: {}",
                userGameId, existingItemAnywhereOpt.isPresent() ? "movido/reordenado en" : "añadido a",
                targetSection.getName(), tierList.getName(), userEmail);

        TierList reloadedTierList = findTierListByPublicIdAndOwnerOrThrow(updatedTierList.getPublicId(), tierList.getOwner());
        return tierListMapper.toTierListResponseDTOWithSections(reloadedTierList);
    }

    @Override
    @Transactional
    public TierListResponseDTO moveItemInTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId, TierListItemMoveRequestDTO itemMoveRequestDTO) {
        User owner = getUserByEmailOrThrow(userEmail);
        logger.info("INICIO moveItemInTierList - User: {}, TierListID: {}, ItemID: {}, TargetSectionID: {}, NewOrder: {}",
                userEmail, tierListPublicId, tierListItemInternalId, itemMoveRequestDTO.getTargetSectionInternalId(), itemMoveRequestDTO.getNewOrder());

        TierList tierList = tierListRepository.findByPublicIdAndOwnerWithSections(tierListPublicId, owner)
                .map(tl -> {
                    initializeTierListDetails(tl); // Carga profunda inicial
                    logger.debug("TierList {} y detalles cargados.", tl.getPublicId());
                    return tl;
                })
                .orElseThrow(() -> new ResourceNotFoundException("TierList con ID público " + tierListPublicId + " no encontrada para el usuario " + owner.getNombreUsuario()));

        final Long finalTierListItemInternalId = tierListItemInternalId;
        TierListItem itemToMove = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> Objects.equals(item.getInternalId(), finalTierListItemInternalId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("ITEM NO ENCONTRADO - TierListItem con ID {} no encontrado en la TierList {}", finalTierListItemInternalId, tierListPublicId);
                    return new ResourceNotFoundException("TierListItem con ID " + finalTierListItemInternalId + " no encontrado en la TierList " + tierListPublicId);
                });
        logger.debug("Item a mover (ID: {}) encontrado. Sección actual ID: {}. UserGameID: {}",
                itemToMove.getInternalId(), itemToMove.getTierSection().getInternalId(), itemToMove.getUserGame() != null ? itemToMove.getUserGame().getInternalId() : "N/A");

        TierSection oldSection = itemToMove.getTierSection();
        TierSection newSection = tierList.getSections().stream()
                .filter(s -> Objects.equals(s.getInternalId(), itemMoveRequestDTO.getTargetSectionInternalId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección destino con ID " + itemMoveRequestDTO.getTargetSectionInternalId() + " no encontrada."));
        logger.debug("Sección original ID: {}, Sección destino ID: {}", oldSection.getInternalId(), newSection.getInternalId());

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            GameList sourceGameList = tierList.getSourceGameList();
            if (sourceGameList != null) Hibernate.initialize(sourceGameList.getUserGames());
            if (sourceGameList == null || itemToMove.getUserGame() == null || !sourceGameList.getUserGames().contains(itemToMove.getUserGame())) {
                throw new InvalidOperationException("El juego asociado a este item ya no pertenece a la GameList origen o hay una inconsistencia.");
            }
        }

        boolean sectionChanged = !Objects.equals(oldSection.getInternalId(), newSection.getInternalId());
        logger.debug("¿Secciones diferentes?: {}", sectionChanged);

        // Paso 1: Actualizar la FK del ítem a la nueva sección.
        itemToMove.setTierSection(newSection);
        logger.debug("itemToMove (ID: {}) FK actualizada a newSection (ID: {}).", itemToMove.getInternalId(), newSection.getInternalId());

        // Paso 2: Guardar el TierListItem individualmente para persistir el cambio de FK.
        try {
            tierListItemRepository.save(itemToMove); // Guardar el cambio de FK
            logger.debug("itemToMove (ID: {}) guardado con nueva sección.", itemToMove.getInternalId());
        } catch (Exception e) {
            logger.error("ERROR al guardar itemToMove (ID: {}) después de setTierSection: {}", itemToMove.getInternalId(), e.getMessage(), e);
            throw e;
        }

        // Paso 3: Hacer flush del EntityManager para asegurar que el UPDATE de la FK del itemToMove
        // se envíe a la BD antes de manipular las colecciones que podrían activar orphanRemoval.
        try {
            entityManager.flush();
            logger.debug("EntityManager flusheado después de guardar itemToMove.");
        } catch (Exception e) {
            logger.error("ERROR durante entityManager.flush() después de guardar itemToMove (ID: {}): {}", itemToMove.getInternalId(), e.getMessage(), e);
            throw e; // Si aquí falla, el problema de estado ya es evidente.
        }


        // Paso 4: Manipular las colecciones en memoria.
        // Como la FK de itemToMove ya (idealmente) apunta a newSection en la BD,
        // quitarlo de oldSection.items no debería causar un orphanRemoval que lo borre.
        if (sectionChanged) {
            if (oldSection != null && oldSection.getItems() != null) {
                boolean removed = oldSection.getItems().removeIf(it -> Objects.equals(it.getInternalId(), itemToMove.getInternalId()));
                logger.debug("ItemToMove (ID: {}) {} de oldSection.items. Tamaño oldSection.items: {}", itemToMove.getInternalId(), removed ? "eliminado" : "NO eliminado", oldSection.getItems().size());
            }
        }

        // Para asegurar que newSection.items tenga la instancia correcta y en la posición correcta:
        List<TierListItem> newSectionItemsList = newSection.getItems();
        newSectionItemsList.removeIf(it -> Objects.equals(it.getInternalId(), itemToMove.getInternalId())); // Quitar cualquier referencia existente por ID

        int targetOrderIdx = itemMoveRequestDTO.getNewOrder();
        if (targetOrderIdx < 0 || targetOrderIdx > newSectionItemsList.size()) {
            newSectionItemsList.add(itemToMove); // Añadir al final
        } else {
            newSectionItemsList.add(targetOrderIdx, itemToMove); // Añadir en la posición específica
        }
        logger.debug("itemToMove (ID: {}) añadido/reinsertado en newSection.items en la posición deseada. Tamaño newSection.items: {}", itemToMove.getInternalId(), newSectionItemsList.size());


        // Paso 5: Reordenar los ítems en las secciones afectadas (actualiza el campo 'itemOrder' en memoria).
        if (sectionChanged && oldSection != null) {
            reorderItemsAndUpdate(oldSection.getItems());
            logger.debug("Items de oldSection (ID: {}) reordenados.", oldSection.getInternalId());
        }
        reorderItemsAndUpdate(newSectionItemsList);
        logger.debug("Items de newSection (ID: {}) reordenados. itemToMove (ID: {}) tiene nuevo order: {}.", newSection.getInternalId(), itemToMove.getInternalId(), itemToMove.getItemOrder());


        // Paso 6: Guardar la entidad raíz (TierList).
        // Esto debería ahora principalmente guardar los cambios de orden y las asociaciones de colección.
        // No debería intentar borrar itemToMove si el flush anterior y la lógica de FK funcionaron.
        logger.debug("Llamando a tierListRepository.saveAndFlush(tierList) para cambios finales...");
        TierList updatedTierList = tierListRepository.saveAndFlush(tierList);
        logger.info("TierList (ID: {}) guardada y flusheada FINALMENTE.", updatedTierList.getPublicId());


        logger.info("Item ID {} (UserGame ID: {}) movido de sección ID {} a sección ID {} (orden final: {}) en TierList '{}' por usuario {}",
                itemToMove.getInternalId(),
                (itemToMove.getUserGame() != null ? itemToMove.getUserGame().getInternalId() : "N/A"),
                (oldSection != null ? oldSection.getInternalId() : "N/A"),
                newSection.getInternalId(),
                itemToMove.getItemOrder(),
                updatedTierList.getName(),
                userEmail);

        initializeTierListDetails(updatedTierList); // Para el DTO de respuesta
        return tierListMapper.toTierListResponseDTOWithSections(updatedTierList);
    }

    @Override
    @Transactional
    public TierListResponseDTO removeItemFromTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId) {
        User owner = getUserByEmailOrThrow(userEmail);
        TierList tierList = findTierListByPublicIdAndOwnerOrThrow(tierListPublicId, owner);

        TierListItem itemToRemove = tierList.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> Objects.equals(item.getInternalId(), tierListItemInternalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("TierListItem con ID " + tierListItemInternalId + " no encontrado en la TierList " + tierListPublicId));

        if (tierList.getType() == TierListType.FROM_GAMELIST) {
            throw new InvalidOperationException("Los juegos no se pueden eliminar directamente de una TierList vinculada a una GameList. Modifica la GameList original.");
        }

        TierSection section = itemToRemove.getTierSection();
        if (section != null) {
            Hibernate.initialize(section.getItems());
            boolean removed = section.getItems().removeIf(i -> i.getInternalId() != null && Objects.equals(i.getInternalId(), itemToRemove.getInternalId()));
            if(removed) {
                reorderItemsAndUpdate(section.getItems());
            }
        } else {
            logger.warn("El ítem ID {} a eliminar no tenía una sección asociada.", tierListItemInternalId);
        }

        TierList updatedTierList = tierListRepository.saveAndFlush(tierList);
        logger.info("Item ID {} (UserGame ID: {}) eliminado de TierList '{}' por usuario {}",
                itemToRemove.getInternalId(), (itemToRemove.getUserGame() != null ? itemToRemove.getUserGame().getInternalId() : "N/A"), tierList.getName(), userEmail);

        TierList reloadedTierList = findTierListByPublicIdAndOwnerOrThrow(updatedTierList.getPublicId(), owner);
        return tierListMapper.toTierListResponseDTOWithSections(reloadedTierList);
    }
}