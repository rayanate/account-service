package com.charlesschwab.accountService.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<?> h2ConsoleServletRegistration() {
        // Use the Jakarta variant of the H2 servlet for Jakarta Servlet API (Spring Boot 4)
        ServletRegistrationBean<?> registration = new ServletRegistrationBean<>(new JakartaWebServlet());
        registration.addUrlMappings("/h2-console/*");
        registration.setLoadOnStartup(1);
        return registration;
    }
}

