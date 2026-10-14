package com.boulangerie.administration.api;

public interface UtilisateurLookupApi {
    record UtilisateurInfo(Long id, String prenom, String nom, String email, String telephone) {}
    UtilisateurInfo getByIdOrThrow(Long id);
}