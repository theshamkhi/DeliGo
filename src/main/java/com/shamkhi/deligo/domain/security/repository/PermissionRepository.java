package com.shamkhi.deligo.domain.security.repository;

import com.shamkhi.deligo.domain.security.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByName(String name);

    Boolean existsByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);
}