package com.shamkhi.deligo.domain.security.repository;

import com.shamkhi.deligo.domain.security.model.AuthProvider;
import com.shamkhi.deligo.domain.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByClientExpediteurId(String clientExpediteurId);

    Optional<User> findByLivreurId(String livreurId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}