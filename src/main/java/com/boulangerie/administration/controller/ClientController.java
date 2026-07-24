//// administration/controller/ClientController.java
//package com.boulangerie.administration.controller;
//
//import com.boulangerie.administration.dto.ClientDto;
//import com.boulangerie.administration.service.ClientService;
//import com.boulangerie.shared.controller.GenericCrudController;
//import com.boulangerie.shared.dto.PageResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/clients")
//@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
//@Tag(name = "Clients", description = "Gestion des clients")
//public class ClientController
//        extends GenericCrudController<Client, ClientDto> {
//
//    private final ClientService clientService;
//
//    public ClientController(ClientService service) {
//        super(service);
//        this.clientService = service;
//    }
//
//    // ===================== MÉTHODES SPÉCIFIQUES =====================
//
//    @Operation(summary = "Rechercher des clients par nom ou prénom")
//    @GetMapping("/search")
//    public ResponseEntity<PageResponse<ClientDto>> search(
//            @RequestParam String q,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return ResponseEntity.ok(clientService.searchByNomOrPrenom(q, PageRequest.of(page, size)));
//    }
//}