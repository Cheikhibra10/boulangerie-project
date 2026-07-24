// shared/service/AbstractCrudService.java
package com.boulangerie.shared.service.impl;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.shared.model.GenericEntity;
import com.boulangerie.shared.repository.GenericRepository;
import com.boulangerie.shared.service.DefaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class AbstractCrudService<T extends GenericEntity<T>, D>
        implements DefaultService<T, D> {

    protected final GenericRepository<T> repository;
    protected final EntityMapper<D, T> mapper;
    protected final Class<T> entityClass;

    // ===================== CREATE =====================
    @Override
    @Transactional
    public D create(D dto) {
        T entity = mapper.toEntity(dto);
        T saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    // ===================== UPDATE =====================
    @Override
    @Transactional
    public D update(Long id, D dto) {
        T entity = getEntityById(id);
        mapper.partialUpdate(entity, dto);
        T updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    // ===================== PATCH =====================
    @Override
    @Transactional
    public D patchFields(Long id, D dto) {
        T entity = getEntityById(id);
        mapper.partialUpdate(entity, dto);
        T updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    // ===================== GET BY ID =====================
    @Override
    @Transactional(readOnly = true)
    public D getById(Long id) {
        T entity = getEntityById(id);
        return mapper.toDto(entity);
    }

    // ===================== GET ALL (PAGINÉ) =====================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<D> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<T> pageResult = repository.findAll(pageable);
        Page<D> dtoPage = pageResult.map(mapper::toDto);
        return toPageResponse(dtoPage);
    }

    // ===================== DELETE =====================
    @Override
    @Transactional
    public D delete(Long id) {
        T entity = getEntityById(id);
        repository.delete(entity);
        return mapper.toDto(entity);
    }

    // ===================== ARCHIVE =====================
    @Override
    @Transactional
    public D archive(Long id) {
        // Par défaut, on supprime. Surcharger dans les sous-classes pour l'archivage logique.
        return delete(id);
    }

    // ===================== RESTORE =====================
    @Override
    @Transactional
    public D restore(Long id) {
        throw new UnsupportedOperationException("Restore non implémenté");
    }

    // ===================== UTILITAIRES =====================
    @Override
    public T getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found id=" + id));
    }

    /**
     * Convertit une Page<D> en PageResponse<D> avec le builder.
     */
    protected PageResponse<D> toPageResponse(Page<D> page) {
        return PageResponse.<D>builder()
                .content(page.getContent())
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }


}