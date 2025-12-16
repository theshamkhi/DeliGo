package com.shamkhi.deligo.domain.client.service;

import com.shamkhi.deligo.application.mapper.ClientExpediteurMapper;
import com.shamkhi.deligo.domain.client.dto.ClientExpediteurDTO;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.infrastructure.exception.DuplicateResourceException;
import com.shamkhi.deligo.infrastructure.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du ClientExpediteurService")
class ClientExpediteurServiceTest {

    @Mock private ClientExpediteurRepository repository;
    @Mock private ClientExpediteurMapper mapper;
    @InjectMocks private ClientExpediteurService service;

    private ClientExpediteur client;
    private ClientExpediteurDTO clientDTO;

    @BeforeEach
    void setUp() {
        client = new ClientExpediteur();
        client.setId("1");
        client.setNom("Dupont");
        client.setEmail("test@test.com");

        clientDTO = new ClientExpediteurDTO();
        clientDTO.setId("1");
        clientDTO.setEmail("test@test.com");
    }

    @Test
    @DisplayName("Devrait créer un client")
    void shouldCreateClient() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(mapper.toEntity(clientDTO)).thenReturn(client);
        when(repository.save(any())).thenReturn(client);
        when(mapper.toDTO(any())).thenReturn(clientDTO);

        ClientExpediteurDTO result = service.createClient(clientDTO);

        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Devrait lever exception si email existe")
    void shouldThrowWhenEmailExists() {
        when(repository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createClient(clientDTO))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Devrait récupérer par ID")
    void shouldGetById() {
        when(repository.findById("1")).thenReturn(Optional.of(client));
        when(mapper.toDTO(client)).thenReturn(clientDTO);

        ClientExpediteurDTO result = service.getClientById("1");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Devrait mettre à jour")
    void shouldUpdate() {
        when(repository.findById("1")).thenReturn(Optional.of(client));
        when(repository.save(any())).thenReturn(client);
        when(mapper.toDTO(any())).thenReturn(clientDTO);

        ClientExpediteurDTO result = service.updateClient("1", clientDTO);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Devrait supprimer")
    void shouldDelete() {
        when(repository.findById("1")).thenReturn(Optional.of(client));

        service.deleteClient("1");

        verify(repository).delete(client);
    }
}