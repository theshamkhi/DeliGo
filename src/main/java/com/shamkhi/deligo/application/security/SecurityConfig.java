package com.shamkhi.deligo.application.security;

import com.shamkhi.deligo.domain.security.oauth2.CustomOAuth2UserService;
import com.shamkhi.deligo.domain.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.shamkhi.deligo.domain.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS ==========
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ========== ADMIN ENDPOINTS ==========
                        .requestMatchers("/admin/**").hasRole("MANAGER")

                        // ========== CLIENTS ENDPOINTS ==========
                        .requestMatchers(HttpMethod.GET, "/clients/**")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/clients")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/clients/**")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/clients/**")
                        .hasRole("MANAGER")

                        // ========== DESTINATAIRES ENDPOINTS ==========
                        .requestMatchers(HttpMethod.GET, "/destinataires/**")
                        .hasAnyRole("MANAGER", "CLIENT", "LIVREUR")
                        .requestMatchers(HttpMethod.POST, "/destinataires")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/destinataires/**")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/destinataires/**")
                        .hasRole("MANAGER")

                        // ========== COLIS ENDPOINTS ==========
                        // GET operations - all authenticated users can read
                        .requestMatchers(HttpMethod.GET, "/colis", "/colis/**")
                        .hasAnyRole("MANAGER", "CLIENT", "LIVREUR")

                        // POST - create colis (CLIENT and MANAGER)
                        .requestMatchers(HttpMethod.POST, "/colis")
                        .hasAnyRole("MANAGER", "CLIENT")

                        // PUT - full update (MANAGER only)
                        .requestMatchers(HttpMethod.PUT, "/colis/**")
                        .hasRole("MANAGER")

                        // PATCH - status updates (MANAGER and LIVREUR)
                        .requestMatchers(HttpMethod.PATCH, "/colis/*/statut")
                        .hasAnyRole("MANAGER", "LIVREUR")

                        // DELETE (MANAGER only)
                        .requestMatchers(HttpMethod.DELETE, "/colis/**")
                        .hasRole("MANAGER")

                        // ========== LIVREURS ENDPOINTS ==========
                        .requestMatchers("/livreurs/**")
                        .hasRole("MANAGER")

                        // ========== ZONES ENDPOINTS ==========
                        .requestMatchers("/zones/**")
                        .hasRole("MANAGER")

                        // ========== PRODUITS ENDPOINTS ==========
                        .requestMatchers(HttpMethod.GET, "/produits/**")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/produits")
                        .hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/produits/**")
                        .hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/produits/**")
                        .hasRole("MANAGER")

                        // ========== ALL OTHER REQUESTS ==========
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",    // React
                "http://localhost:4200",    // Angular
                "http://localhost:8080"     // Local development
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}