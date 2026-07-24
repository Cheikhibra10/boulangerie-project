// administration/repository/UtilisateurRepository.java
package com.boulangerie.administration.repository;
import com.boulangerie.administration.model.RoleUtilisateur;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends GenericRepository<Utilisateur> {

    /**
     * Recherche un utilisateur par son email (unique)
     * Utilisé pour : authentification, vérification d'unicité
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     * Utilisé pour : validation à la création (US-09)
     */
    boolean existsByEmail(String email);

    /**
     * Recherche les utilisateurs par rôle
     * Utilisé pour : liste des Managers, Caissiers, etc.
     */
//    List<Utilisateur> findByRole(RoleUtilisateur role);

    /**
     * Recherche les utilisateurs actifs par rôle (paginé)
     * Utilisé pour : affichage des utilisateurs actifs
     */
//    Page<Utilisateur> findByActifTrueAndRole(RoleUtilisateur role, Pageable pageable);

    /**
     * Recherche tous les utilisateurs actifs (paginé)
     */
    Page<Utilisateur> findByActifTrue(Pageable pageable);

    /**
     * Recherche les utilisateurs par rôle, avec pagination
     */
//    Page<Utilisateur> findByRole(RoleUtilisateur role, Pageable pageable);

    Optional<Utilisateur> findByKeycloakId(String keycloakId);
}