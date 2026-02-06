package com.shamkhi.deligo.domain.colis.service;

import com.shamkhi.deligo.application.mapper.ColisMapper;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.model.Destinataire;
import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.*;
import com.shamkhi.deligo.domain.livraison.model.Zone;
import com.shamkhi.deligo.domain.colis.repository.ColisRepository;
import com.shamkhi.deligo.domain.colis.repository.ColisProduitRepository;
import com.shamkhi.deligo.domain.colis.repository.HistoriqueLivraisonRepository;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.domain.client.repository.DestinataireRepository;
import com.shamkhi.deligo.domain.livraison.repository.LivreurRepository;
import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
import com.shamkhi.deligo.domain.produit.repository.ProduitRepository;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ColisService {

    private final ColisRepository colisRepository;
    private final HistoriqueLivraisonRepository historiqueRepository;
    private final ColisProduitRepository colisProduitRepository;
    private final ClientExpediteurRepository clientRepository;
    private final UserRepository userRepository;
    private final DestinataireRepository destinataireRepository;
    private final LivreurRepository livreurRepository;
    private final ZoneRepository zoneRepository;
    private final ProduitRepository produitRepository;
    private final ColisMapper colisMapper;

    // ============ USER-AWARE METHODS ============

    public Page<ColisDTO> getAllColisForUser(Authentication auth, Pageable pageable) {
        log.info("Récupération des colis pour l'utilisateur: {}", auth.getName());

        if (hasRole(auth, "ROLE_MANAGER")) {
            return getAllColis(pageable);
        } else if (hasRole(auth, "ROLE_LIVREUR")) {
            User user = getUserByAuth(auth);
            if (user.getLivreur() != null) {
                return getColisByLivreur(user.getLivreur().getId(), pageable);
            }
        } else if (hasRole(auth, "ROLE_CLIENT")) {
            User user = getUserByAuth(auth);
            if (user.getClientExpediteur() != null) {
                return getColisByClient(user.getClientExpediteur().getId(), pageable);
            }
        }

        return Page.empty(pageable);
    }

    public ColisDTO getColisByIdForUser(String id, Authentication auth) {
        log.info("Récupération du colis {} pour l'utilisateur: {}", id, auth.getName());

        ColisDTO colis = getColisById(id);

        // MANAGER can see all
        if (hasRole(auth, "ROLE_MANAGER")) {
            return colis;
        }

        User user = getUserByAuth(auth);

        // LIVREUR can only see assigned colis
        if (hasRole(auth, "ROLE_LIVREUR")) {
            String userLivreurId = user.getLivreur() != null ? user.getLivreur().getId() : null;
            if (userLivreurId == null || !userLivreurId.equals(colis.getLivreurId())) {
                throw new AccessDeniedException("Vous n'avez pas accès à ce colis");
            }
            return colis;
        }

        // CLIENT can only see their colis
        if (hasRole(auth, "ROLE_CLIENT")) {
            String userClientId = user.getClientExpediteur() != null ? user.getClientExpediteur().getId() : null;
            if (userClientId == null || !userClientId.equals(colis.getClientExpediteurId())) {
                throw new AccessDeniedException("Vous n'avez pas accès à ce colis");
            }
            return colis;
        }

        throw new AccessDeniedException("Accès refusé");
    }

    public Page<ColisDTO> searchColisForUser(String keyword, Authentication auth, Pageable pageable) {
        log.info("Recherche de colis pour l'utilisateur: {}", auth.getName());

        if (hasRole(auth, "ROLE_MANAGER")) {
            return searchColis(keyword, pageable);
        }

        return searchColis(keyword, pageable);
    }

    @Transactional
    public ColisDTO createColisForUser(CreateColisRequest request, Authentication auth) {
        log.info("Création d'un colis pour l'utilisateur: {}", auth.getName());

        // If CLIENT, ensure they're creating for themselves
        if (hasRole(auth, "ROLE_CLIENT") && !hasRole(auth, "ROLE_MANAGER")) {
            User user = getUserByAuth(auth);
            String userClientId = user.getClientExpediteur() != null ? user.getClientExpediteur().getId() : null;
            if (userClientId == null || !request.getClientExpediteurId().equals(userClientId)) {
                throw new AccessDeniedException("Vous ne pouvez créer des colis que pour vous-même");
            }
        }

        return createColis(request);
    }

    @Transactional
    public ColisDTO updateStatutForUser(String id, UpdateStatutRequest request, Authentication auth) {
        log.info("Mise à jour du statut du colis {} pour l'utilisateur: {}", id, auth.getName());

        // LIVREUR can only update their assigned colis
        if (hasRole(auth, "ROLE_LIVREUR") && !hasRole(auth, "ROLE_MANAGER")) {
            User user = getUserByAuth(auth);
            ColisDTO colis = getColisById(id);

            String userLivreurId = user.getLivreur() != null ? user.getLivreur().getId() : null;
            if (userLivreurId == null || !userLivreurId.equals(colis.getLivreurId())) {
                throw new AccessDeniedException("Vous ne pouvez modifier que vos colis assignés");
            }

            // Set modifiePar if not provided
            if (request.getModifiePar() == null || request.getModifiePar().isEmpty()) {
                request.setModifiePar(user.getUsername());
            }
        }

        // Set modifiePar for MANAGER if not provided
        if (hasRole(auth, "ROLE_MANAGER")) {
            if (request.getModifiePar() == null || request.getModifiePar().isEmpty()) {
                request.setModifiePar(auth.getName());
            }
        }

        return updateStatut(id, request);
    }

    public ColisStatisticsResponse getStatisticsForUser(Authentication auth) {
        log.info("Récupération des statistiques pour l'utilisateur: {}", auth.getName());

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return getStatisticsByUser(auth.getName(), roles);
    }

    public List<ColisDTO> getOverdueColisForUser(Authentication auth) {
        log.info("Récupération des colis en retard pour l'utilisateur: {}", auth.getName());

        if (hasRole(auth, "ROLE_MANAGER")) {
            return getOverdueColis();
        }

        if (hasRole(auth, "ROLE_LIVREUR")) {
            User user = getUserByAuth(auth);
            if (user.getLivreur() != null) {
                String userLivreurId = user.getLivreur().getId();
                return getOverdueColis().stream()
                        .filter(c -> userLivreurId.equals(c.getLivreurId()))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }

    public Page<ColisDTO> getColisByPrioriteForUser(PrioriteColis priorite, Authentication auth, Pageable pageable) {
        log.info("Récupération des colis par priorité pour l'utilisateur: {}", auth.getName());

        if (hasRole(auth, "ROLE_MANAGER")) {
            return getColisByMultipleCriteria(null, priorite, null, null, null, pageable);
        }

        if (hasRole(auth, "ROLE_LIVREUR")) {
            User user = getUserByAuth(auth);
            if (user.getLivreur() != null) {
                return getColisByMultipleCriteria(null, priorite, null, null, user.getLivreur().getId(), pageable);
            }
        }

        return Page.empty(pageable);
    }

    // ============ ORIGINAL PUBLIC METHODS ============

    public Page<ColisDTO> getAllColis(Pageable pageable) {
        log.info("Récupération de tous les colis avec pagination");
        return colisRepository.findAll(pageable).map(colisMapper::toDTO);
    }

    public ColisDTO getColisById(String id) {
        log.info("Récupération du colis avec id: {}", id);
        Colis colis = findColisById(id);
        return colisMapper.toDTO(colis);
    }

    public Page<ColisDTO> searchColis(String keyword, Pageable pageable) {
        log.info("Recherche de colis avec mot-clé: {}", keyword);
        return colisRepository.searchByKeyword(keyword, pageable).map(colisMapper::toDTO);
    }

    public Page<ColisDTO> getColisByMultipleCriteria(
            StatutColis statut,
            PrioriteColis priorite,
            String zoneId,
            String ville,
            String livreurId,
            Pageable pageable) {
        log.info("Filtrage des colis avec critères multiples");
        return colisRepository.findByMultipleCriteria(statut, priorite, zoneId, ville, livreurId, pageable)
                .map(colisMapper::toDTO);
    }

    public Page<ColisDTO> getColisByClientExpediteur(String clientId, Pageable pageable) {
        log.info("Récupération des colis du client expéditeur: {}", clientId);
        return colisRepository.findByClientExpediteurId(clientId, pageable).map(colisMapper::toDTO);
    }

    public Page<ColisDTO> getColisByClient(String clientId, Pageable pageable) {
        return getColisByClientExpediteur(clientId, pageable);
    }

    public Page<ColisDTO> getColisByDestinataire(String destinataireId, Pageable pageable) {
        log.info("Récupération des colis du destinataire: {}", destinataireId);
        return colisRepository.findByDestinataireId(destinataireId, pageable).map(colisMapper::toDTO);
    }

    public Page<ColisDTO> getColisByLivreur(String livreurId, Pageable pageable) {
        log.info("Récupération des colis du livreur: {}", livreurId);
        return colisRepository.findByLivreurId(livreurId, pageable).map(colisMapper::toDTO);
    }

    @Transactional
    public ColisDTO createColis(CreateColisRequest request) {
        log.info("Création d'un nouveau colis");

        ClientExpediteur client = clientRepository.findById(request.getClientExpediteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Client expéditeur non trouvé"));

        Destinataire destinataire = destinataireRepository.findById(request.getDestinataireId())
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire non trouvé"));

        Zone zone = null;
        if (request.getZoneId() != null) {
            zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée"));
        }

        Colis colis = colisMapper.toEntity(request);

        colis.setClientExpediteur(client);
        colis.setDestinataire(destinataire);
        if (zone != null) {
            colis.setZone(zone);
        }

        colis = colisRepository.save(colis);

        createHistorique(colis, StatutColis.CREE, "Colis créé", null);

        log.info("Colis créé avec succès, id: {}", colis.getId());
        return colisMapper.toDTO(colis);
    }

    @Transactional
    public ColisDTO updateColis(String id, UpdateColisRequest request) {
        log.info("Mise à jour du colis: {}", id);

        Colis colis = findColisById(id);
        StatutColis oldStatut = colis.getStatut();

        colisMapper.updateEntity(request, colis);

        if (request.getLivreurId() != null) {
            colis.setLivreur(livreurRepository.findById(request.getLivreurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé")));
        }

        if (request.getZoneId() != null) {
            colis.setZone(zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        }

        // Si le statut a changé, créer un historique
        if (request.getStatut() != null && request.getStatut() != oldStatut) {
            createHistorique(colis, request.getStatut(), "Statut mis à jour", null);
            updateDatesByStatut(colis, request.getStatut());
        }

        colis = colisRepository.save(colis);
        log.info("Colis mis à jour avec succès");
        return colisMapper.toDTO(colis);
    }

    @Transactional
    public ColisDTO updateStatut(String id, UpdateStatutRequest request) {
        log.info("Mise à jour du statut du colis: {} vers {}", id, request.getStatut());

        Colis colis = findColisById(id);
        StatutColis oldStatut = colis.getStatut();

        if (oldStatut == request.getStatut()) {
            log.warn("Le statut est déjà: {}", request.getStatut());
            return colisMapper.toDTO(colis);
        }

        colis.setStatut(request.getStatut());
        updateDatesByStatut(colis, request.getStatut());

        colis = colisRepository.save(colis);
        createHistorique(colis, request.getStatut(), request.getCommentaire(), request.getModifiePar());

        log.info("Statut mis à jour avec succès");
        return colisMapper.toDTO(colis);
    }

    @Transactional
    public void deleteColis(String id) {
        log.info("Suppression du colis: {}", id);
        Colis colis = findColisById(id);
        colisRepository.delete(colis);
        log.info("Colis supprimé avec succès");
    }

    // Gestion de l'historique
    public List<HistoriqueLivraisonDTO> getHistoriqueByColis(String colisId) {
        log.info("Récupération de l'historique du colis: {}", colisId);
        List<HistoriqueLivraison> historiques = historiqueRepository.findByColisIdOrderByDateChangementDesc(colisId);
        return colisMapper.toHistoriqueDTOList(historiques);
    }

    // Gestion des produits
    public List<ColisProduitDTO> getProduitsByColis(String colisId) {
        log.info("Récupération des produits du colis: {}", colisId);
        List<ColisProduit> produits = colisProduitRepository.findByColisId(colisId);
        return colisMapper.toColisProduitDTOList(produits);
    }

    @Transactional
    public ColisProduitDTO addProduitToColis(String colisId, AddProduitToColisRequest request) {
        log.info("Ajout d'un produit au colis: {}", colisId);

        Colis colis = findColisById(colisId);

        if (!canModifyProducts(colis.getStatut())) {
            throw new IllegalStateException(
                    "Impossible d'ajouter des produits au colis avec le statut: " + colis.getStatut().getLibelle()
            );
        }

        ColisProduit colisProduit = new ColisProduit();
        colisProduit.setColis(colis);
        colisProduit.setProduit(produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé")));
        colisProduit.setQuantite(request.getQuantite());
        colisProduit.setPrix(request.getPrix());

        colisProduit = colisProduitRepository.save(colisProduit);
        log.info("Produit ajouté au colis avec succès");
        return colisMapper.toColisProduitDTO(colisProduit);
    }

    @Transactional
    public void removeProduitFromColis(String colisProduitId) {
        log.info("Suppression d'un produit du colis: {}", colisProduitId);

        ColisProduit colisProduit = colisProduitRepository.findById(colisProduitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit du colis non trouvé"));

        if (!canModifyProducts(colisProduit.getColis().getStatut())) {
            throw new IllegalStateException(
                    "Impossible de supprimer des produits du colis avec le statut: " +
                            colisProduit.getColis().getStatut().getLibelle()
            );
        }

        colisProduitRepository.deleteById(colisProduitId);
    }

    private boolean canModifyProducts(StatutColis statut) {
        return statut == StatutColis.CREE || statut == StatutColis.EN_STOCK;
    }

    public ColisStatisticsResponse getDetailedStatistics() {
        log.info("Calcul des statistiques détaillées des colis");

        return ColisStatisticsResponse.builder()
                .total(colisRepository.count())
                .cree(colisRepository.countByStatut(StatutColis.CREE))
                .collecte(colisRepository.countByStatut(StatutColis.COLLECTE))
                .enStock(colisRepository.countByStatut(StatutColis.EN_STOCK))
                .enTransit(colisRepository.countByStatut(StatutColis.EN_TRANSIT))
                .livre(colisRepository.countByStatut(StatutColis.LIVRE))
                .annule(colisRepository.countByStatut(StatutColis.ANNULE))
                .retourne(colisRepository.countByStatut(StatutColis.RETOURNE))
                .build();
    }

    public ColisStatisticsResponse getStatisticsByUser(String username, List<String> roles) {
        log.info("Calcul des statistiques pour l'utilisateur: {}", username);

        // Si MANAGER, retourner toutes les stats
        if (roles.contains("ROLE_MANAGER")) {
            return getDetailedStatistics();
        }

        // Si LIVREUR, filtrer par livreur
        if (roles.contains("ROLE_LIVREUR")) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

            if (user.getLivreur() != null) {
                return getStatisticsByLivreurId(user.getLivreur().getId());
            }
        }

        // Si CLIENT, filtrer par client
        if (roles.contains("ROLE_CLIENT")) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

            if (user.getClientExpediteur() != null) {
                return getStatisticsByClientId(user.getClientExpediteur().getId());
            }
        }

        // Par défaut, retourner des stats vides
        return ColisStatisticsResponse.builder()
                .total(0).cree(0).collecte(0).enStock(0)
                .enTransit(0).livre(0).annule(0).retourne(0)
                .build();
    }

    public ColisStatisticsResponse getStatisticsByLivreurId(String livreurId) {
        return ColisStatisticsResponse.builder()
                .total(colisRepository.countByLivreurId(livreurId))
                .cree(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.CREE))
                .collecte(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.COLLECTE))
                .enStock(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.EN_STOCK))
                .enTransit(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.EN_TRANSIT))
                .livre(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.LIVRE))
                .annule(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.ANNULE))
                .retourne(colisRepository.countByLivreurIdAndStatut(livreurId, StatutColis.RETOURNE))
                .build();
    }

    private ColisStatisticsResponse getStatisticsByClientId(String clientId) {
        return ColisStatisticsResponse.builder()
                .total(colisRepository.countByClientExpediteurId(clientId))
                .cree(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.CREE))
                .collecte(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.COLLECTE))
                .enStock(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.EN_STOCK))
                .enTransit(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.EN_TRANSIT))
                .livre(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.LIVRE))
                .annule(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.ANNULE))
                .retourne(colisRepository.countByClientExpediteurIdAndStatut(clientId, StatutColis.RETOURNE))
                .build();
    }

    public List<ColisDTO> getOverdueColis() {
        log.info("Récupération des colis en retard");
        List<StatutColis> excludedStatuses = Arrays.asList(StatutColis.LIVRE, StatutColis.ANNULE, StatutColis.RETOURNE);
        List<Colis> overdue = colisRepository.findOverdueColis(LocalDateTime.now(), excludedStatuses);
        return colisMapper.toDTOList(overdue);
    }

    // ============ HELPER METHODS ============

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private User getUserByAuth(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    private Colis findColisById(String id) {
        return colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis non trouvé avec l'id: " + id));
    }

    private void createHistorique(Colis colis, StatutColis statut, String commentaire, String modifiePar) {
        HistoriqueLivraison historique = new HistoriqueLivraison();
        historique.setColis(colis);
        historique.setStatut(statut);
        historique.setDateChangement(LocalDateTime.now());
        historique.setCommentaire(commentaire);
        historique.setModifiePar(modifiePar);
        historiqueRepository.save(historique);
    }

    private void updateDatesByStatut(Colis colis, StatutColis statut) {
        switch (statut) {
            case COLLECTE:
                if (colis.getDateCollecte() == null) {
                    colis.setDateCollecte(LocalDateTime.now());
                }
                break;
            case LIVRE:
                if (colis.getDateLivraison() == null) {
                    colis.setDateLivraison(LocalDateTime.now());
                }
                break;
        }
    }

    private List<ColisStatisticsDTO> mapToStatistics(List<Object[]> results) {
        List<ColisStatisticsDTO> stats = new ArrayList<>();
        for (Object[] result : results) {
            ColisStatisticsDTO stat = new ColisStatisticsDTO();
            stat.setEntityId((String) result[0]);
            stat.setEntityName(result.length > 1 ? (String) result[1] : "");
            stat.setCount((Long) result[result.length - 2]);
            stat.setTotalWeight((BigDecimal) result[result.length - 1]);
            stats.add(stat);
        }
        return stats;
    }
}