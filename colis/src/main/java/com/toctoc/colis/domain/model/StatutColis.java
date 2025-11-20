package com.toctoc.colis.domain.model;

public enum StatutColis {
    CREE("Créé"),
    COLLECTE("Collecté"),
    EN_STOCK("En stock"),
    EN_TRANSIT("En transit"),
    LIVRE("Livré"),
    RETOURNE("Retourné"),
    ANNULE("Annulé");

    private final String libelle;

    StatutColis(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}