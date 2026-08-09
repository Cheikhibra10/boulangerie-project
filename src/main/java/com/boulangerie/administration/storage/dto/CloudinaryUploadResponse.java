package com.boulangerie.administration.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CloudinaryUploadResponse(

        @JsonProperty("public_id")
        String publicId,

        @JsonProperty("secure_url")
        String secureUrl,

        String result

) {
}