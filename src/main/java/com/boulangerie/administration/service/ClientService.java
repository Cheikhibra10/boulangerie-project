//// administration/service/ClientService.java
//package com.boulangerie.administration.service;
//
//import com.boulangerie.administration.dto.ClientDto;
//import com.boulangerie.shared.dto.PageResponse;
//import com.boulangerie.shared.service.DefaultService;
//import org.springframework.data.domain.Pageable;
//
//public interface ClientService extends DefaultService<Client, ClientDto> {
//
//    /**
//     * Recherche des clients par nom/prénom (pour ajout à abonnement)
//     */
//    PageResponse<ClientDto> searchByNomOrPrenom(String searchTerm, Pageable pageable);
//
//    Long findClientIdOrThrow(Long clientId);
//}