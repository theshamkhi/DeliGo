package com.toctoc.client.mapper;

import com.toctoc.client.domain.model.ClientExpediteur;
import com.toctoc.client.dto.ClientExpediteurDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientExpediteurMapper {

    ClientExpediteurDTO toDTO(ClientExpediteur client);

    List<ClientExpediteurDTO> toDTOList(List<ClientExpediteur> clients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    ClientExpediteur toEntity(ClientExpediteurDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    void updateEntity(ClientExpediteurDTO dto, @MappingTarget ClientExpediteur client);
}