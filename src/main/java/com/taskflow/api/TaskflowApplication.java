package com.taskflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The single entry point of the entire application.
 *
 * <p>{@code @SpringBootApplication} is a convenience annotation that bundles three others:
 * <ul>
 *   <li>{@code @Configuration} — marks this class as a source of Spring bean definitions.</li>
 *   <li>{@code @EnableAutoConfiguration} — the "magic". Spring Boot inspects the classpath and
 *       auto-configures sensible beans: because spring-boot-starter-web is present it sets up an
 *       embedded Tomcat + Spring MVC; because data-jpa is present it wires a DataSource, an
 *       EntityManager, transactions, etc. We only override what we need.</li>
 *   <li>{@code @ComponentScan} — tells Spring to scan THIS package ({@code com.taskflow.api}) and
 *       everything beneath it for components ({@code @Service}, {@code @RestController}, ...).
 *       This is precisely why our package layout sits under com.taskflow.api: so every feature
 *       package gets discovered automatically.</li>
 * </ul>
 *
 * <p>The Spring lifecycle in one sentence: {@code SpringApplication.run} boots the
 * "ApplicationContext" (the container that holds all our beans), runs auto-configuration,
 * starts the embedded web server, and then blocks, keeping the app alive to serve requests.
 */
@SpringBootApplication
public class TaskflowApplication {

    public static void main(String[] args) {
        // Hand control to Spring Boot: build the context, start Tomcat, begin serving.
        SpringApplication.run(TaskflowApplication.class, args);
    }
}
