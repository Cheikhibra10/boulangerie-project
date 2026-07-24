package com.boulangerie.achats.repository;

import com.boulangerie.achats.model.LigneAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    List<LigneAchat> findByAchatId(Long achatId);

    @Query("SELECT l FROM LigneAchat l JOIN FETCH l.ingredient WHERE l.achat.id = :achatId")
    List<LigneAchat> findByAchatIdWithIngredient(@Param("achatId") Long achatId);
}