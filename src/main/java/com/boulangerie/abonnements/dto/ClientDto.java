package com.boulangerie.abonnements.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private Long id;

    private String prenom;

    private String nom;

    private String telephone;

    private Boolean actif;
}
