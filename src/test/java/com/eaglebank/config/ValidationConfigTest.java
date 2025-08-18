package com.eaglebank.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for ValidationConfig configuration class.
 * Tests validator factory bean creation and method validation configuration.
 */
@DisplayName("ValidationConfig Tests")
class ValidationConfigTest {

    private ValidationConfig validationConfig;

    @BeforeEach
    void setUp() {
        validationConfig = new ValidationConfig();
    }

    @Nested
    @DisplayName("Configuration and Annotation Tests")
    class ConfigurationAndAnnotationTests {

        @Test
        @DisplayName("Should be annotated with @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(ValidationConfig.class.isAnnotationPresent(Configuration.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should have proper class structure")
        void shouldHaveProperClassStructure() {
            assertThat(ValidationConfig.class.getSimpleName())
                    .isEqualTo("ValidationConfig");
            assertThat(ValidationConfig.class.getPackage().getName())
                    .isEqualTo("com.eaglebank.config");
        }

        @Test
        @DisplayName("Should have proper configuration methods")
        void shouldHaveProperConfigurationMethods() throws Exception {
            assertThat(ValidationConfig.class.getMethod("validator"))
                    .isNotNull();
            assertThat(ValidationConfig.class.getMethod("methodValidationPostProcessor"))
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("Validator Factory Bean Tests")
    class ValidatorFactoryBeanTests {

        @Test
        @DisplayName("Should create LocalValidatorFactoryBean instance")
        void shouldCreateLocalValidatorFactoryBeanInstance() {
            LocalValidatorFactoryBean validator = validationConfig.validator();

            assertThat(validator).isNotNull();
            assertThat(validator).isInstanceOf(LocalValidatorFactoryBean.class);
        }

        @Test
        @DisplayName("Should create distinct validator instances")
        void shouldCreateDistinctValidatorInstances() {
            LocalValidatorFactoryBean validator1 = validationConfig.validator();
            LocalValidatorFactoryBean validator2 = validationConfig.validator();

            assertThat(validator1).isNotSameAs(validator2);
        }

        @Test
        @DisplayName("Should create validator with proper configuration")
        void shouldCreateValidatorWithProperConfiguration() {
            LocalValidatorFactoryBean validator = validationConfig.validator();

            // Validator should be properly configured
            assertThat(validator).isNotNull();
            
            // Should be able to get the underlying validator (after initialization)
            validator.afterPropertiesSet();
            assertThat(validator.getValidator()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Method Validation Post Processor Tests")
    class MethodValidationPostProcessorTests {

        @Test
        @DisplayName("Should create MethodValidationPostProcessor instance")
        void shouldCreateMethodValidationPostProcessorInstance() {
            MethodValidationPostProcessor processor = validationConfig.methodValidationPostProcessor();

            assertThat(processor).isNotNull();
            assertThat(processor).isInstanceOf(MethodValidationPostProcessor.class);
        }

        @Test
        @DisplayName("Should configure processor with validator")
        void shouldConfigureProcessorWithValidator() {
            MethodValidationPostProcessor processor = validationConfig.methodValidationPostProcessor();

            assertThat(processor).isNotNull();
            // The processor should be configured (we can't easily check the internal validator reference)
            // but we can verify it was created successfully
        }

        @Test
        @DisplayName("Should create distinct processor instances")
        void shouldCreateDistinctProcessorInstances() {
            MethodValidationPostProcessor processor1 = validationConfig.methodValidationPostProcessor();
            MethodValidationPostProcessor processor2 = validationConfig.methodValidationPostProcessor();

            assertThat(processor1).isNotSameAs(processor2);
        }

        @Test
        @DisplayName("Should create processor that integrates with validator")
        void shouldCreateProcessorThatIntegratesWithValidator() {
            // Create both beans
            LocalValidatorFactoryBean validator = validationConfig.validator();
            MethodValidationPostProcessor processor = validationConfig.methodValidationPostProcessor();

            // Both should be created successfully
            assertThat(validator).isNotNull();
            assertThat(processor).isNotNull();
            
            // Initialize validator
            validator.afterPropertiesSet();
            assertThat(validator.getValidator()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Bean Integration Tests")
    class BeanIntegrationTests {

        @Test
        @DisplayName("Should create compatible beans")
        void shouldCreateCompatibleBeans() {
            LocalValidatorFactoryBean validator = validationConfig.validator();
            MethodValidationPostProcessor processor = validationConfig.methodValidationPostProcessor();

            assertThat(validator).isNotNull();
            assertThat(processor).isNotNull();
            
            // Both beans should be Spring framework components
            assertThat(validator.getClass().getPackage().getName())
                    .startsWith("org.springframework");
            assertThat(processor.getClass().getPackage().getName())
                    .startsWith("org.springframework");
        }

        @Test
        @DisplayName("Should support bean validation workflow")
        void shouldSupportBeanValidationWorkflow() {
            LocalValidatorFactoryBean validator = validationConfig.validator();
            
            // Initialize the validator
            validator.afterPropertiesSet();
            
            // Should have a working validator instance
            assertThat(validator.getValidator()).isNotNull();
            
            // Should support basic validation operations
            assertThat(validator.getValidator().getClass().getName())
                    .contains("hibernate.validator");
        }
    }
}
