package com.boulangerie.shared.websocket;

import com.boulangerie.shared.security.KeycloakJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Connexion WebSocket refusée : en-tête Authorization manquant");
            throw new InvalidBearerTokenException("Token JWT requis pour se connecter");
        }

        try {
            Jwt jwt = jwtDecoder.decode(authHeader.substring(7));
            AbstractAuthenticationToken authentication = jwtAuthenticationConverter.convert(jwt);
            accessor.setUser(authentication);
        } catch (JwtException ex) {
            log.warn("Connexion WebSocket refusée : token invalide ou expiré — {}", ex.getMessage());
            throw new InvalidBearerTokenException("Token JWT invalide ou expiré");
        }

        return message;
    }
}