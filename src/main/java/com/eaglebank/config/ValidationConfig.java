package com.eaglebank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Configuration class for Bean Validation to ensure consistent validation across the application.
 * This configuration ensures that:
 * 1. Bean Validation annotations are processed consistently
 * 2. Method-level validation is enabled for service layer
 * 3. Validation messages are standardized
 */
@Configuration
public class ValidationConfig {

    /**
     * Configures the validator factory for Bean Validation.
     * This ensures consistent validation behavior across all layers.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Enables method-level validation for service layer methods.
     * This allows validation of method parameters and return values.
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
}
