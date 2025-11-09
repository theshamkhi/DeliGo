# 📦 TocToc - Smart Delivery Management System

> Système de gestion logistique moderne pour SmartLogi - Gestion complète des livraisons de colis à travers le Maroc

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Description

**TocToc** est une application web de gestion logistique développée pour moderniser et automatiser les opérations de livraison de SmartLogi. Le système remplace la gestion manuelle par fichiers Excel par une solution complète de suivi et traçabilité des colis.

### ✨ Fonctionnalités Principales

- 📦 **Gestion des Colis** : Création, suivi et mise à jour du statut des colis
- 👥 **Gestion des Clients** : Expéditeurs et destinataires
- 🚚 **Gestion des Livreurs** : Assignation et suivi des tournées
- 🗺️ **Zones Géographiques** : Organisation par régions
- 📊 **Statistiques** : Tableaux de bord et rapports
- 🔍 **Recherche Avancée** : Filtrage multi-critères
- 📝 **Historique Complet** : Traçabilité de chaque colis

---

## 🛠️ Technologies Utilisées

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.2.0** - Framework
- **Spring Data JPA** - Persistance des données
- **Spring Validation** - Validation des données
- **PostgreSQL** - Base de données
- **Liquibase** - Gestion des migrations
- **MapStruct** - Mapping DTO ↔ Entity
- **Lombok** - Réduction du boilerplate
- **Maven** - Gestion de projet

### Documentation & API
- **Swagger/OpenAPI 3** - Documentation interactive
- **Spring REST** - API RESTful

---

## 🏗️ Architecture

Le projet suit une architecture **DDD (Domain-Driven Design)** simplifiée :

```
src/main/java/com/toctoc/toctoc2/
├── domain/              # Logique métier
│   ├── colis/          # Module Colis
│   ├── client/         # Module Client
│   ├── livraison/      # Module Livraison
│   └── produit/        # Module Produit
├── application/         # Couche application
│   ├── controller/     # Controllers REST
│   ├── mapper/         # Mappers DTO
│   └── config/         # Configuration
├── infrastructure/      # Infrastructure
│   ├── exception/      # Gestion erreurs
│   ├── validation/     # Validation custom
│   └── email/          # Service email
└── shared/             # Utilitaires
```

---

## 🚀 Installation

### Prérequis

- Java 17 ou supérieur
- Maven 3.8+
- PostgreSQL 15+

### Option 1 : Installation Locale

1. **Cloner le repository**
```bash
git clone https://github.com/theshamkhi/TocTocV2.git
cd TocToc2
```

2. **Créer la base de données**
```sql
CREATE DATABASE TocTocV2;
```

3. **Configurer application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/TocTocV2
    username: postgres
    password: votre_mot_de_passe
```

4. **Compiler et lancer**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Accéder à l'application**
- API : http://localhost:8080/api/v1
- Swagger UI : http://localhost:8080/api/v1/swagger-ui.html

---

## 📸 Captures d'écran

### Swagger UI - Documentation Interactive



### Diagramme UML - Modèle de Données


---

## 📡 Endpoints API

### Colis
- `GET /colis` - Liste paginée
- `POST /colis` - Créer un colis
- `GET /colis/{id}` - Détails d'un colis
- `PUT /colis/{id}` - Mettre à jour
- `PATCH /colis/{id}/statut` - Changer le statut
- `DELETE /colis/{id}` - Supprimer
- `GET /colis/search?keyword=...` - Recherche
- `GET /colis/filter?statut=...&priorite=...` - Filtrage
- `GET /colis/{id}/historique` - Historique complet

### Clients & Destinataires
- `GET /clients` - Liste des clients
- `POST /clients` - Créer un client
- `GET /destinataires` - Liste des destinataires
- `POST /destinataires` - Créer un destinataire

### Livreurs
- `GET /livreurs` - Liste des livreurs
- `GET /livreurs/actifs` - Livreurs actifs uniquement
- `POST /livreurs` - Créer un livreur

### Zones & Produits
- `GET /zones` - Liste des zones
- `POST /zones` - Créer une zone
- `GET /produits` - Liste des produits
- `POST /produits` - Créer un produit