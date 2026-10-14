package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.BulletinPaieDto;
import com.boulangerie.rh.dto.BulletinPaieFilter;
import com.boulangerie.rh.dto.GenererBulletinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BulletinPaieService {

    BulletinPaieDto generer(GenererBulletinRequest request);

    BulletinPaieDto getById(Long id);

    BulletinPaieDto valider(Long id);

    BulletinPaieDto marquerPaye(Long id, LocalDate datePaiement);

    BulletinPaieDto annuler(Long id);

    void delete(Long id);

    Page<BulletinPaieDto> search(BulletinPaieFilter filter, Pageable pageable);
}