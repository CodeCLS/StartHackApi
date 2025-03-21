package com.lol.ml.starthackapi;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSocket
<<<<<<< HEAD
public class WebSocketConfig implements WebSocketConfigurer, WebMvcConfigurer {

=======
public class WebSocketConfig implements WebSocketConfigurer{
>>>>>>> 8a93245564068652ed412c4a68f4d9463240a6bf
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LiveTextWebSocketHandler(), "/live-text")
               .setAllowedOrigins("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
