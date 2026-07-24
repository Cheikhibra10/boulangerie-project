// shared/model/GenericEntity.java
package com.boulangerie.shared.model;

public interface GenericEntity<T> {
    Long getId();
    T createNewInstance();
}