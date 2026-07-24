//// administration/service/impl/ClientServiceImpl.java
//package com.boulangerie.administration.service.Impl;
//
//import com.boulangerie.administration.dto.ClientDto;
//import com.boulangerie.administration.service.ClientService;
//import com.boulangerie.shared.dto.PageResponse;
//import com.boulangerie.shared.exception.EntityNotFoundException;
//import com.boulangerie.shared.service.impl.AbstractCrudService;
//import com.boulangerie.shared.utils.PageUtils;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@Transactional
//public class ClientServiceImpl
//        extends AbstractCrudService<Client, ClientDto>
//        implements ClientService {
//    private final ClientRepository repository;
//    public ClientServiceImpl(ClientRepository repository,
//                             ClientMapper mapper) {
//        super(repository, mapper, Client.class);
//        this.repository = repository;
//    }
//
//    @Override
//    public PageResponse<ClientDto> searchByNomOrPrenom(String searchTerm, Pageable pageable) {
//        return PageUtils.toPageResponse(
//                repository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
//                                searchTerm, searchTerm, pageable)
//                        .map(mapper::toDto)
//        );
//    }
//
//    @Override
//    public ClientDto archive(Long id) {
//        Client client = getEntityById(id);
//        client.setActif(false);
//        return mapper.toDto(repository.save(client));
//    }
//
//    @Override
//    public ClientDto restore(Long id) {
//        Client client = getEntityById(id);
//        client.setActif(true);
//        return mapper.toDto(repository.save(client));
//    }
//
//    @Override
//    public Long findClientIdOrThrow(Long clientId) {
//        if (repository.existsById(clientId)) {
//            throw new EntityNotFoundException("Client introuvable");
//        }
//        return clientId;
//    }
//}