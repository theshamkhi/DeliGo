package com.shamkhi.deligo.application.mapper;

import com.shamkhi.deligo.domain.produit.dto.ProduitDTO;
import com.shamkhi.deligo.domain.produit.model.Produit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProduitMapper {

    ProduitDTO toDTO(Produit produit);

    List<ProduitDTO> toDTOList(List<Produit> produits);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    Produit toEntity(ProduitDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    void updateEntity(ProduitDTO dto, @MappingTarget Produit produit);
}