package com.shamkhi.deligo.domain.security.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Le nom de la permission est obligatoire")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // READ_COLIS, WRITE_COLIS, MANAGE_LIVREURS, etc.

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "resource", length = 50)
    private String resource; // COLIS, LIVREUR, ZONE, CLIENT, etc.

    @Column(name = "action", length = 50)
    private String action; // READ, WRITE, UPDATE, DELETE, MANAGE
}