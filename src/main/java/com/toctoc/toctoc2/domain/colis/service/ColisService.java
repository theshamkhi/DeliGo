package com.toctoc.toctoc2.domain.colis.service;

import com.toctoc.toctoc2.application.mapper.ColisMapper;
import com.toctoc.toctoc2.domain.colis.dto.*;
import com.toctoc.toctoc2.domain.colis.model.*;
import com.toctoc.toctoc2.domain.colis.repository.ColisRepository;
import com.toctoc.toctoc2.domain.colis.repository.ColisProduitRepository;
import com.toctoc.toctoc2.domain.colis.repository.HistoriqueLivraisonRepository;
import com.toctoc.toctoc2.domain.client.repository.ClientExpediteurRepository;
import com.toctoc.toctoc2.domain.client.repository.DestinataireRepository;
import com.toctoc.toctoc2.domain.livraison.repository.LivreurRepository;
import com.toctoc.toctoc2.domain.livraison.repository.ZoneRepository;
import com.toctoc.toctoc2.domain.produit.repository.ProduitRepository;
import com.toctoc.toctoc2.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ColisService {

    private final ColisRepository colisRepository;
    private final HistoriqueLivraisonRepository historiqueRepository;
    private final ColisProduitRepository colisProduitRepository;
    private final ClientExpediteurRepository clientRepository;
    private final DestinataireRepository destinataireRepository;
    private final LivreurRepository livreurRepository;
    private final ZoneRepository zoneRepository;
    private final ProduitRepository produitRepository;
    private final ColisMapper colisMapper;

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

        Colis colis = colisMapper.toEntity(request);

        // Associer les relations
        colis.setClientExpediteur(clientRepository.findById(request.getClientExpediteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Client expéditeur non trouvé")));

        colis.setDestinataire(destinataireRepository.findById(request.getDestinataireId())
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire non trouvé")));

        if (request.getZoneId() != null) {
            colis.setZone(zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        }

        colis = colisRepository.save(colis);

        // Créer le premier historique
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

        // Gérer les relations si modifiées
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
    public void updateStatut(String id, UpdateStatutRequest request) {
        log.info("Mise à jour du statut du colis: {} vers {}", id, request.getStatut());

        Colis colis = findColisById(id);
        StatutColis oldStatut = colis.getStatut();

        if (oldStatut == request.getStatut()) {
            log.warn("Le statut est déjà: {}", request.getStatut());
            return;
        }

        colis.setStatut(request.getStatut());
        updateDatesByStatut(colis, request.getStatut());

        colisRepository.save(colis);
        createHistorique(colis, request.getStatut(), request.getCommentaire(), request.getModifiePar());

        log.info("Statut mis à jour avec succès");
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
        colisProduitRepository.deleteById(colisProduitId);
    }

    // Statistiques
    public List<ColisStatisticsDTO> getStatisticsByLivreur() {
        log.info("Calcul des statistiques par livreur");
        List<Object[]> results = colisRepository.countAndSumWeightByLivreur();
        return mapToStatistics(results);
    }

    public List<ColisStatisticsDTO> getStatisticsByZone() {
        log.info("Calcul des statistiques par zone");
        List<Object[]> results = colisRepository.countAndSumWeightByZone();
        return mapToStatistics(results);
    }

    public List<ColisDTO> getOverdueColis() {
        log.info("Récupération des colis en retard");
        List<StatutColis> excludedStatuses = Arrays.asList(StatutColis.LIVRE, StatutColis.ANNULE, StatutColis.RETOURNE);
        List<Colis> overdue = colisRepository.findOverdueColis(LocalDateTime.now(), excludedStatuses);
        return colisMapper.toDTOList(overdue);
    }

    // Méthodes privées
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