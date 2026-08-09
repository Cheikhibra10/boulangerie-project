package com.boulangerie.achats.dto;

import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.*;
import com.boulangerie.achats.repository.AchatSpecifications;
import lombok.AllArgsConstructor;
import lombok.*;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchatSearchCriteria {

    private String reference;

    private Long fournisseurId;

    private String fournisseurNom;

    private StatutAchat statut;

    private StatutReception statutReception;

    private StatutPaiement statutPaiement;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private BigDecimal montantMin;

    private BigDecimal montantMax;
}