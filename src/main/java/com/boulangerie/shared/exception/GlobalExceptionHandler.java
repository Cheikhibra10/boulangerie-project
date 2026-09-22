package com.boulangerie.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionSchema> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionSchema> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionSchema> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication error on {} : {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    // --- Accès refusé : @PreAuthorize/@Secured qui échoue, ou UnauthorizedException métier ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionSchema> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Accès refusé sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Accès refusé", request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionSchema> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Accès refusé sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    // --- Violation de contrainte réelle levée par Hibernate/JPA (unicité, NOT NULL, FK...) ---
    // NB : org.springframework.dao.DataIntegrityViolationException, PAS un type maison —
    // c'est bien celui que Spring Data lève lui-même depuis repository.save()/delete().
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionSchema> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn(
                "Violation de contrainte de données sur {} : {}",
                request.getRequestURI(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "Cette opération viole une contrainte de données (doublon ou référence invalide)",
                request.getRequestURI()
        );
    }

    // --- Violation de contrainte jakarta.validation (ex: @Validated sur @RequestParam/@PathVariable) ---
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionSchema> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage())
        );

        return new ResponseEntity<>(
                new ExceptionSchema(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation échouée",
                        request.getRequestURI(),
                        errors
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionSchema> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // --- Toute autre BusinessException métier non catégorisée plus précisément ci-dessus ---
    // Le type du paramètre DOIT correspondre au type déclaré dans @ExceptionHandler, sinon
    // Spring plante en essayant d'invoquer la méthode avec l'exception réellement levée
    // (c'était le bug ici : le paramètre était typé ConflictException au lieu de BusinessException).
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionSchema> handleBusinessConflict(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ExceptionSchema> handleInternalServerError(InternalServerErrorException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    // --- Échec de génération Excel : message métier réel préservé, plutôt que noyé dans le catch-all ---
    @ExceptionHandler(ExcelExportException.class)
    public ResponseEntity<ExceptionSchema> handleExcelExport(ExcelExportException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("[{}] Échec d'export Excel sur {} : {}", errorId, request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                new ExceptionSchema(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Échec de génération du fichier Excel (réf. " + errorId + ")",
                        request.getRequestURI(),
                        errorId
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // --- Gestion des erreurs JSON mal formé ---
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionSchema> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "JSON invalide ou mal formaté",
                request.getRequestURI()
        );
    }

    // --- Erreur de type de paramètre ---
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionSchema> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'. Expected type: " + ex.getRequiredType().getSimpleName();
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    // --- Gestion des routes inexistantes ---
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionSchema> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found: " + request.getRequestURI(), request.getRequestURI());
    }

    // --- Méthode HTTP non supportée ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionSchema> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed: " + ex.getMethod(), request.getRequestURI());
    }

    // --- Catch-all fallback : le SEUL endroit qui doit encore produire un 500 "générique".
    // Chaque 500 est désormais associé à un errorId unique, loggé avec la stacktrace complète
    // ET renvoyé au client, pour pouvoir relier un ticket support/rapport utilisateur à la
    // ligne de log exacte sans exposer le détail interne dans la réponse HTTP.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionSchema> handleAllExceptions(Exception ex, HttpServletRequest request) {

        String errorId = UUID.randomUUID().toString();

        log.error(
                "[{}] Erreur non gérée sur {} {} : {}",
                errorId,
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return new ResponseEntity<>(
                new ExceptionSchema(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Une erreur interne est survenue (réf. " + errorId + ")",
                        request.getRequestURI(),
                        errorId
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // --- Méthode utilitaire ---
    private ResponseEntity<ExceptionSchema> buildResponse(HttpStatus status, String message, String path) {
        ExceptionSchema error = new ExceptionSchema(status.value(), message, path);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionSchema> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(
                new ExceptionSchema(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation échouée",
                        request.getRequestURI(),
                        errors
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}