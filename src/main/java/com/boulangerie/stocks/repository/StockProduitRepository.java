// stocks/repository/StockProduitRepository.java
package com.boulangerie.stocks.repository;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.stocks.model.StockProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockProduitRepository extends JpaRepository<StockProduit, Long> {

    Optional<StockProduit> findByProduitId(Long produitId);

    Optional<StockProduit> findByProduit(Produit produit);
}