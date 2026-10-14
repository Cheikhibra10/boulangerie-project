package com.boulangerie.administration.api;

public interface UtilisateurLookupApi {
    record UtilisateurInfo(Long id, String prenom, String nom, String email) {}
    UtilisateurInfo getByIdOrThrow(Long id);
}