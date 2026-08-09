package com.boulangerie.administration.storage.dto;

import lombok.Builder;

@Builder
public record ImageUploadResult(String url, String publicId) {

}