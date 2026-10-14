package com.boulangerie.rh.repository;

public interface AbsenceParEmployeProjection {
    Long getEmployeId();
    String getPrenom();
    String getNom();
    long getNombreAbsences();
    long getJoursAbsents();

    // default method for convenience
    default String getNomComplet() {
        return getPrenom() + " " + getNom();
    }
}