package com.speakBuddy.speackBuddy_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //Punto de entrada para conectar el WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") //Permite conexiones desde cualquier origen
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //Prefijo de mensajes que van desde el cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");

        /*Prefijo del mensaje que va del servidor al cliente
        el /queue es para mensajes privados y el /topic para publicos*/
        registry.enableSimpleBroker("/queue", "/topic");

        //Prefijos para mensajes dirigidos a usuarios concretos
        registry.setUserDestinationPrefix("/user");
    }
}
