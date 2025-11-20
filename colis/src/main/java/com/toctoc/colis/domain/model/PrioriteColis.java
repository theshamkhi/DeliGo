package com.toctoc.colis.domain.model;

public enum PrioriteColis {
    NORMALE("Normale"),
    URGENT("Urgent"),
    TRES_URGENT("Très urgent");

    private final String libelle;

    PrioriteColis(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}