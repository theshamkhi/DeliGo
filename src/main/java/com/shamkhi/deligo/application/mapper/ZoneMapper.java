package com.shamkhi.deligo.application.mapper;

import com.shamkhi.deligo.domain.livraison.dto.ZoneDTO;
import com.shamkhi.deligo.domain.livraison.model.Zone;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZoneMapper {

    ZoneDTO toDTO(Zone zone);

    List<ZoneDTO> toDTOList(List<Zone> zones);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    Zone toEntity(ZoneDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    void updateEntity(ZoneDTO dto, @MappingTarget Zone zone);
}