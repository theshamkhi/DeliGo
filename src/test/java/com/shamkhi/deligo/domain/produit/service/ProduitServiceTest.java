package com.shamkhi.deligo.domain.produit.service;

import com.shamkhi.deligo.application.mapper.ProduitMapper;
import com.shamkhi.deligo.domain.produit.dto.ProduitDTO;
import com.shamkhi.deligo.domain.produit.model.Produit;
import com.shamkhi.deligo.domain.produit.repository.ProduitRepository;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du ProduitService")
class ProduitServiceTest {

    @Mock private ProduitRepository repository;
    @Mock private ProduitMapper mapper;
    @InjectMocks private ProduitService service;

    private Produit produit;
    private ProduitDTO produitDTO;

    @BeforeEach
    void setUp() {
        produit = new Produit();
        produit.setId("1");
        produit.setNom("Laptop Dell");
        produit.setCategorie("Électronique");
        produit.setPrix(new BigDecimal("5000"));

        produitDTO = new ProduitDTO();
        produitDTO.setId("1");
        produitDTO.setNom("Laptop Dell");
        produitDTO.setCategorie("Électronique");
    }

    @Test
    @DisplayName("Devrait récupérer tous les produits")
    void shouldGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produit> page = new PageImpl<>(Arrays.asList(produit));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(produitDTO);

        Page<ProduitDTO> result = service.getAllProduits(pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait récupérer un produit par ID")
    void shouldGetById() {
        when(repository.findById("1")).thenReturn(Optional.of(produit));
        when(mapper.toDTO(produit)).thenReturn(produitDTO);

        ProduitDTO result = service.getProduitById("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    @DisplayName("Devrait lever exception si produit non trouvé")
    void shouldThrowWhenNotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProduitById("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produit non trouvé");
    }

    @Test
    @DisplayName("Devrait créer un produit")
    void shouldCreate() {
        when(mapper.toEntity(produitDTO)).thenReturn(produit);
        when(repository.save(any())).thenReturn(produit);
        when(mapper.toDTO(any())).thenReturn(produitDTO);

        ProduitDTO result = service.createProduit(produitDTO);

        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour un produit")
    void shouldUpdate() {
        when(repository.findById("1")).thenReturn(Optional.of(produit));
        when(repository.save(any())).thenReturn(produit);
        when(mapper.toDTO(any())).thenReturn(produitDTO);

        ProduitDTO result = service.updateProduit("1", produitDTO);

        assertThat(result).isNotNull();
        verify(mapper).updateEntity(produitDTO, produit);
    }

    @Test
    @DisplayName("Devrait supprimer un produit")
    void shouldDelete() {
        when(repository.findById("1")).thenReturn(Optional.of(produit));

        service.deleteProduit("1");

        verify(repository).delete(produit);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé")
    void shouldSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produit> page = new PageImpl<>(Arrays.asList(produit));
        when(repository.searchByKeyword("Dell", pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(produitDTO);

        Page<ProduitDTO> result = service.searchProduits("Dell", pageable);

        assertThat(result).isNotEmpty();
    }
}
