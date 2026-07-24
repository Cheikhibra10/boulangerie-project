// shared/service/DefaultService.java
package com.boulangerie.shared.service;

import com.boulangerie.shared.dto.PageResponse;

    public interface DefaultService<T, D> {

    D create(D dto);

    D update(Long id, D dto);

    D patchFields(Long id, D dto);

    D getById(Long id);

    PageResponse<D> findAll(int page, int size);

    D delete(Long id);

    D archive(Long id);

    D restore(Long id);

    T getEntityById(Long id);

}