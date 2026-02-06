package com.shamkhi.deligo.application.mapper;

import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.Colis;
import com.shamkhi.deligo.domain.colis.model.ColisProduit;
import com.shamkhi.deligo.domain.colis.model.HistoriqueLivraison;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ColisMapper {

    @Mapping(target = "livreurId", source = "livreur.id")
    @Mapping(target = "livreurNom", expression = "java(getLivreurNom(colis))")
    @Mapping(target = "clientExpediteurId", source = "clientExpediteur.id")
    @Mapping(target = "clientExpediteurNom", expression = "java(getClientNom(colis))")
    @Mapping(target = "destinataireId", source = "destinataire.id")
    @Mapping(target = "destinataireNom", expression = "java(getDestinataireNom(colis))")
    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneNom", source = "zone.nom")
    ColisDTO toDTO(Colis colis);

    List<ColisDTO> toDTOList(List<Colis> colis);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", constant = "CREE")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "dateCollecte", ignore = true)
    @Mapping(target = "dateLivraison", ignore = true)
    @Mapping(target = "livreur", ignore = true)
    @Mapping(target = "clientExpediteur", ignore = true)
    @Mapping(target = "destinataire", ignore = true)
    @Mapping(target = "zone", ignore = true)
    Colis toEntity(CreateColisRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "clientExpediteur", ignore = true)
    @Mapping(target = "destinataire", ignore = true)
    @Mapping(target = "livreur", ignore = true)
    @Mapping(target = "zone", ignore = true)
    void updateEntity(UpdateColisRequest request, @MappingTarget Colis colis);

    default String getLivreurNom(Colis colis) {
        if (colis.getLivreur() == null) return null;
        return colis.getLivreur().getNom() + " " + colis.getLivreur().getPrenom();
    }

    default String getClientNom(Colis colis) {
        if (colis.getClientExpediteur() == null) return null;
        return colis.getClientExpediteur().getNom() + " " + colis.getClientExpediteur().getPrenom();
    }

    default String getDestinataireNom(Colis colis) {
        if (colis.getDestinataire() == null) return null;
        return colis.getDestinataire().getNom() + " " + colis.getDestinataire().getPrenom();
    }

    @Mapping(target = "statut", source = "statut")
    HistoriqueLivraisonDTO toHistoriqueDTO(HistoriqueLivraison historique);

    List<HistoriqueLivraisonDTO> toHistoriqueDTOList(List<HistoriqueLivraison> historiques);

    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitNom", source = "produit.nom")
    ColisProduitDTO toColisProduitDTO(ColisProduit colisProduit);

    List<ColisProduitDTO> toColisProduitDTOList(List<ColisProduit> colisProduits);
}