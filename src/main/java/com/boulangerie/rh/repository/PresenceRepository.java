package com.boulangerie.rh.repository;

import com.boulangerie.rh.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PresenceRepository
        extends JpaRepository<Presence, Long>,
                JpaSpecificationExecutor<Presence> {

    boolean existsByEmployeIdAndDate(Long employeId, LocalDate date);
}