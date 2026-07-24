// administration/dto/ClientDto.java
package com.boulangerie.administration.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ClientDto extends PersonneDto {
    // Tous les champs hérités de PersonneDto
}