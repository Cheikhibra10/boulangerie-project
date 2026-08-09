// ventes/mapper/VenteMapper.java
package com.boulangerie.ventes.mapper;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.ventes.dto.VenteDto;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.VenteBoutique;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", uses = {LigneVenteMapper.class, PaiementMapper.class})
public abstract class VenteMapper implements EntityMapper<VenteDto, VenteBoutique> {

    @Override
    @Mapping(target = "numero", source = ".", qualifiedByName = "numero")

    @Mapping(target = "caissier", source = "utilisateur", qualifiedByName = "caissier")

    @Mapping(target = "heure", source = "createdAt", qualifiedByName = "heure")

    @Mapping(target = "articlesVendus", source = ".", qualifiedByName = "articlesVendus")

    @Mapping(target = "paiement", source = "paiement")

    public abstract VenteDto toDto(VenteBoutique entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(VenteDto dto, @MappingTarget VenteBoutique entity);

    @Named("numero")
    protected String numero(VenteBoutique vente) {
        return "VENTE-%06d".formatted(vente.getId());
    }

    @Named("caissier")
    protected String caissier(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }

    @Named("heure")
    protected LocalTime heure(Instant createdAt) {
        return createdAt == null ? null : createdAt.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    @Named("articlesVendus")
    protected BigDecimal articlesVendus(VenteBoutique vente) {
        return vente.getLignes()
                .stream()
                .map(LigneVenteBoutique::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}