package com.shamkhi.deligo.domain.produit.service;

import com.shamkhi.deligo.application.mapper.ProduitMapper;
import com.shamkhi.deligo.domain.produit.dto.ProduitDTO;
import com.shamkhi.deligo.domain.produit.model.Produit;
import com.shamkhi.deligo.domain.produit.repository.ProduitRepository;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProduitService {

    private final ProduitRepository repository;
    private final ProduitMapper mapper;

    public Page<ProduitDTO> getAllProduits(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ProduitDTO getProduitById(String id) {
        return mapper.toDTO(findProduitById(id));
    }

    public Page<ProduitDTO> searchProduits(String keyword, Pageable pageable) {
        return repository.searchByKeyword(keyword, pageable).map(mapper::toDTO);
    }

    @Transactional
    public ProduitDTO createProduit(ProduitDTO dto) {
        Produit produit = mapper.toEntity(dto);
        produit = repository.save(produit);
        return mapper.toDTO(produit);
    }

    @Transactional
    public ProduitDTO updateProduit(String id, ProduitDTO dto) {
        Produit produit = findProduitById(id);
        mapper.updateEntity(dto, produit);
        produit = repository.save(produit);
        return mapper.toDTO(produit);
    }

    @Transactional
    public void deleteProduit(String id) {
        Produit produit = findProduitById(id);
        repository.delete(produit);
    }

    private Produit findProduitById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec id: " + id));
    }
}