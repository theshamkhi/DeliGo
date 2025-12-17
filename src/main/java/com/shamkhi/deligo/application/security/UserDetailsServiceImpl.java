package com.shamkhi.deligo.application.security;

import com.shamkhi.deligo.domain.security.model.Permission;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        if (!user.getActif()) {
            throw new UsernameNotFoundException("Compte désactivé: " + username);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(!user.getActif())
                .credentialsExpired(false)
                .disabled(!user.getActif())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Ajouter les rôles
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            // Ajouter les permissions de chaque rôle
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }

        return authorities;
    }
}