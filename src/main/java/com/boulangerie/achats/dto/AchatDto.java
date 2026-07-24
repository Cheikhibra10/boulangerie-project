package com.boulangerie.achats.dto;

import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.StatutPaiement;
import com.boulangerie.achats.model.StatutReception;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchatDto extends AbstractAuditingDto {
    Long id;
    Long fournisseurId;
    String fournisseurNom;
    private StatutAchat statutAchat;
    private StatutReception statutReception;
    private StatutPaiement statutPaiement;
    List<LigneAchatDto> lignes;
    List<PaiementFournisseurDto> paiements;
}