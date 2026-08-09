package com.boulangerie.administration.storage.service;

import com.boulangerie.administration.storage.dto.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    ImageUploadResult upload(MultipartFile image);

    ImageUploadResult replace(String oldPublicId, MultipartFile newImage);

    void delete(String publicId);

}