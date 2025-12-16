package com.shamkhi.deligo.application.mapper;

import com.shamkhi.deligo.domain.livraison.dto.LivreurDTO;
import com.shamkhi.deligo.domain.livraison.model.Livreur;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LivreurMapper {

    @Mapping(target = "zoneAssigneeId", source = "zoneAssignee.id")
    @Mapping(target = "zoneAssigneeNom", source = "zoneAssignee.nom")
    LivreurDTO toDTO(Livreur livreur);

    List<LivreurDTO> toDTOList(List<Livreur> livreurs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "zoneAssignee", ignore = true)
    Livreur toEntity(LivreurDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "zoneAssignee", ignore = true)
    void updateEntity(LivreurDTO dto, @MappingTarget Livreur livreur);
}