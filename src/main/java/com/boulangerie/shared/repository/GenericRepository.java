// shared/repository/GenericRepository.java
package com.boulangerie.shared.repository;

import com.boulangerie.shared.model.GenericEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericRepository<T extends GenericEntity<T>>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}