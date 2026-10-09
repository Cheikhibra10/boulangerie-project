package com.boulangerie.administration.security;

import com.boulangerie.shared.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS_PER_EMAIL = 5;
    private static final int MAX_ATTEMPTS_PER_IP = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptWindow> emailAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptWindow> ipAttempts = new ConcurrentHashMap<>();

    public void checkAllowed(String email, String clientIp) {
        cleanupIfNeeded();

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = clientIp == null ? "unknown" : clientIp;

        if (isBlocked(emailAttempts, normalizedEmail, MAX_ATTEMPTS_PER_EMAIL)) {
            throw new RateLimitExceededException(
                    "Trop de tentatives de connexion pour ce compte. Réessayez dans 15 minutes."
            );
        }

        if (isBlocked(ipAttempts, ip, MAX_ATTEMPTS_PER_IP)) {
            throw new RateLimitExceededException(
                    "Trop de tentatives depuis cette adresse IP. Réessayez dans 15 minutes."
            );
        }
    }

    public void recordFailure(String email, String clientIp) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = clientIp == null ? "unknown" : clientIp;

        emailAttempts.compute(normalizedEmail, (k, v) -> increment(v));
        ipAttempts.compute(ip, (k, v) -> increment(v));
    }

    public void recordSuccess(String email, String clientIp) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = clientIp == null ? "unknown" : clientIp;

        emailAttempts.remove(normalizedEmail);
        // Do not fully clear IP on one success (shared office IP)
        AttemptWindow ipWindow = ipAttempts.get(ip);
        if (ipWindow != null && ipWindow.isExpired()) {
            ipAttempts.remove(ip);
        }
    }

    private boolean isBlocked(Map<String, AttemptWindow> map, String key, int max) {
        AttemptWindow window = map.get(key);
        if (window == null) return false;
        if (window.isExpired()) {
            map.remove(key);
            return false;
        }
        return window.count() >= max;
    }

    private AttemptWindow increment(AttemptWindow current) {
        Instant now = Instant.now();
        if (current == null || current.isExpired()) {
            return new AttemptWindow(1, now.plus(WINDOW));
        }
        return new AttemptWindow(current.count() + 1, current.expiresAt());
    }

    private void cleanupIfNeeded() {
        // Lightweight cleanup to avoid unbounded growth
        if (emailAttempts.size() > 10_000 || ipAttempts.size() > 10_000) {
            purgeExpired(emailAttempts);
            purgeExpired(ipAttempts);
        }
    }

    private void purgeExpired(ConcurrentHashMap<String, AttemptWindow> map) {
        Iterator<Map.Entry<String, AttemptWindow>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isExpired()) {
                it.remove();
            }
        }
    }

    private record AttemptWindow(int count, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}