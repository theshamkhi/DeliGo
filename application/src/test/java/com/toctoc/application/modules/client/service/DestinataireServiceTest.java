package com.toctoc.application.modules.client.service;

import com.toctoc.client.domain.service.DestinataireService;
import com.toctoc.client.dto.DestinataireDTO;
import com.toctoc.client.domain.model.Destinataire;
import com.toctoc.client.domain.repository.DestinataireRepository;
import com.toctoc.client.mapper.DestinataireMapper;
import com.toctoc.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du DestinataireService")
class DestinataireServiceTest {

    @Mock private DestinataireRepository repository;
    @Mock private DestinataireMapper mapper;
    @InjectMocks private DestinataireService service;

    private Destinataire destinataire;
    private DestinataireDTO destinataireDTO;

    @BeforeEach
    void setUp() {
        destinataire = new Destinataire();
        destinataire.setId("1");
        destinataire.setNom("Martin");
        destinataire.setPrenom("Marie");
        destinataire.setTelephone("0698765432");

        destinataireDTO = new DestinataireDTO();
        destinataireDTO.setId("1");
        destinataireDTO.setNom("Martin");
        destinataireDTO.setTelephone("0698765432");
    }

    @Test
    @DisplayName("Devrait récupérer tous les destinataires")
    void shouldGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Destinataire> page = new PageImpl<>(Arrays.asList(destinataire));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(destinataireDTO);

        Page<DestinataireDTO> result = service.getAllDestinataires(pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait récupérer un destinataire par ID")
    void shouldGetById() {
        when(repository.findById("1")).thenReturn(Optional.of(destinataire));
        when(mapper.toDTO(destinataire)).thenReturn(destinataireDTO);

        DestinataireDTO result = service.getDestinataireById("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    @DisplayName("Devrait lever exception si destinataire non trouvé")
    void shouldThrowWhenNotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDestinataireById("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Destinataire non trouvé");
    }

    @Test
    @DisplayName("Devrait créer un destinataire")
    void shouldCreate() {
        when(mapper.toEntity(destinataireDTO)).thenReturn(destinataire);
        when(repository.save(any())).thenReturn(destinataire);
        when(mapper.toDTO(any())).thenReturn(destinataireDTO);

        DestinataireDTO result = service.createDestinataire(destinataireDTO);

        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour un destinataire")
    void shouldUpdate() {
        when(repository.findById("1")).thenReturn(Optional.of(destinataire));
        when(repository.save(any())).thenReturn(destinataire);
        when(mapper.toDTO(any())).thenReturn(destinataireDTO);

        DestinataireDTO result = service.updateDestinataire("1", destinataireDTO);

        assertThat(result).isNotNull();
        verify(mapper).updateEntity(destinataireDTO, destinataire);
    }

    @Test
    @DisplayName("Devrait supprimer un destinataire")
    void shouldDelete() {
        when(repository.findById("1")).thenReturn(Optional.of(destinataire));

        service.deleteDestinataire("1");

        verify(repository).delete(destinataire);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé")
    void shouldSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Destinataire> page = new PageImpl<>(Arrays.asList(destinataire));
        when(repository.searchByKeyword("Martin", pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(destinataireDTO);

        Page<DestinataireDTO> result = service.searchDestinataires("Martin", pageable);

        assertThat(result).isNotEmpty();
    }
}