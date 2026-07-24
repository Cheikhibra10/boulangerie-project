package com.boulangerie.shared.model;

/**
 * Interface que doivent implémenter les entités représentant un utilisateur
 * afin de fournir leur identifiant pour l'audit.
 */
public interface IdentifiableUser {
    Long getId();
}