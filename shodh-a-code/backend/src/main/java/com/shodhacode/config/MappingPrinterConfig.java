package com.shodhacode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * Prints all request mappings at startup to help debug missing/incorrect endpoints.
 */
@Configuration
public class MappingPrinterConfig {
    private static final Logger logger = LoggerFactory.getLogger(MappingPrinterConfig.class);

    @Bean
    public CommandLineRunner printMappings(RequestMappingHandlerMapping mapping) {
        return args -> {
            logger.info("****** Registered request mappings ******");
            mapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
                logger.info("Mapping: {}  -> {}#{}",
                        requestMappingInfo.getPatternsCondition(),
                        handlerMethod.getBeanType().getSimpleName(),
                        handlerMethod.getMethod().getName());
            });
            logger.info("****** End of mappings ******");
        };
    }
}
