package com.toctoc.colis.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.toctoc.colis",
        "com.toctoc.livraison",
        "com.toctoc.produit"
})
@EntityScan(basePackages = {
        "com.toctoc.colis.domain.model",
        "com.toctoc.livraison.domain.model",
        "com.toctoc.produit.domain.model"
})
@EnableJpaRepositories(basePackages = {
        "com.toctoc.colis.domain.repository",
        "com.toctoc.livraison.domain.repository",
        "com.toctoc.produit.domain.repository"
})
public class ColisModuleConfig {
}
