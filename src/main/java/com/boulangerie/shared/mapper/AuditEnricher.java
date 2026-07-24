//package com.boulangerie.shared.mapper;
//
//import com.boulangerie.shared.dto.AbstractAuditingDto;
//import com.boulangerie.shared.model.AbstractAuditingEntity;
//import com.boulangerie.shared.service.UserDirectoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//
//@Component
//@RequiredArgsConstructor
//public class AuditEnricher {
//
//    private final UserDirectoryService userDirectoryService;
//    private static final DateTimeFormatter FORMATTER =
//            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
//                    .withZone(ZoneId.of("Africa/Dakar"));
//
//    public void enrich(
//            AbstractAuditingEntity entity,
//            AbstractAuditingDto dto
//    ) {
//
//
//        dto.setCreatedAt(FORMATTER.format(entity.getCreatedAt()));
//        dto.setUpdatedAt(FORMATTER.format(entity.getUpdatedAt()));
//
//        dto.setCreatedBy(resolveName(entity.getCreatedBy()));
//        dto.setUpdatedBy(resolveName(entity.getUpdatedBy()));
//    }
//
//    private String resolveName(String userId) {
//
//        if (userId == null || userId.isBlank()) {
//            return null;
//        }
//        try{
//            return userDirectoryService.getUser(userId).fullName();
//        }catch (Exception ex){
//            return userId;
//        }
//    }
//}