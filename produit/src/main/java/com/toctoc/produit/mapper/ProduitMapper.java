package com.toctoc.produit.mapper;

import com.toctoc.produit.dto.ProduitDTO;
import com.toctoc.produit.domain.model.Produit;
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