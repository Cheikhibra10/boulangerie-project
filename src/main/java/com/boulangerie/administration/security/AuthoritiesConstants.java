package com.boulangerie.administration.security;

import com.boulangerie.administration.model.RoleUtilisateur;

public final class AuthoritiesConstants {


    public static final String ADMIN =
            RoleUtilisateur.ADMIN.name();


    public static final String MANAGER =
            RoleUtilisateur.MANAGER.name();


    public static final String CAISSIER =
            RoleUtilisateur.CAISSIER.name();


    public static final String BOULANGER =
            RoleUtilisateur.GESTIONNAIRE_PRODUCTION.name();


    private AuthoritiesConstants() {}

}