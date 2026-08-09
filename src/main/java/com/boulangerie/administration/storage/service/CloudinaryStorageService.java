package com.boulangerie.administration.storage.service;


import com.boulangerie.administration.exception.ImageStorageException;
import com.boulangerie.administration.storage.config.CloudinaryProperties;
import com.boulangerie.administration.storage.dto.CloudinaryUploadResponse;
import com.boulangerie.administration.storage.dto.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService implements ImageStorageService {

    private final RestClient cloudinaryRestClient;
    private final CloudinaryProperties properties;

    @Override
    public ImageUploadResult upload(MultipartFile file) {

        validate(file);

        long timestamp = Instant.now().getEpochSecond();

        String signature = sign(
                "folder=" + properties.getFolder()
                        + "&timestamp=" + timestamp
        );

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add(
                "file",
                new ByteArrayResource(read(file)) {

                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                }
        );

        body.add("api_key", properties.getApiKey());
        body.add("timestamp", timestamp);
        body.add("folder", properties.getFolder());
        body.add("signature", signature);

        CloudinaryUploadResponse response =
                cloudinaryRestClient.post()
                        .uri(uploadUrl())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .body(CloudinaryUploadResponse.class);

        if (response == null) {
            throw new ImageStorageException(
                    "Cloudinary n'a retourné aucune réponse"
            );
        }

        if (response.publicId() == null ||
                response.secureUrl() == null) {

            throw new ImageStorageException(
                    "Réponse Cloudinary invalide"
            );
        }

        return new ImageUploadResult(
                response.secureUrl(),
                response.publicId()
        );
    }

    @Override
    public ImageUploadResult replace(
            String publicId,
            MultipartFile file
    ) {

        validate(file);

        ImageUploadResult uploaded = upload(file);

        if (publicId != null && !publicId.isBlank()) {
            delete(publicId);
        }

        return uploaded;
    }

    @Override
    public void delete(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        long timestamp = Instant.now().getEpochSecond();

        String signature = sign(
                "public_id=" + publicId
                        + "&timestamp=" + timestamp
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("public_id", publicId);
        body.add("timestamp", String.valueOf(timestamp));
        body.add("api_key", properties.getApiKey());
        body.add("signature", signature);

        cloudinaryRestClient.post()
                .uri(destroyUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ImageStorageException("Image vide");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new ImageStorageException(
                    "Le fichier doit être une image"
            );
        }
    }

    private byte[] read(MultipartFile file) {

        try {
            return file.getBytes();

        } catch (IOException e) {

            throw new ImageStorageException(
                    "Impossible de lire le fichier"
            );
        }
    }

    private String uploadUrl() {

        return "https://api.cloudinary.com/v1_1/"
                + properties.getCloudName()
                + "/image/upload";
    }

    private String destroyUrl() {

        return "https://api.cloudinary.com/v1_1/"
                + properties.getCloudName()
                + "/image/destroy";
    }

    private String sign(String canonicalString) {

        return sha1(
                canonicalString
                        + properties.getApiSecret()
        );
    }

    private String sha1(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-1");

            byte[] hash =
                    digest.digest(
                            value.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result = new StringBuilder(
                    hash.length * 2
            );

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-1 algorithm unavailable",
                    e
            );
        }
    }
}