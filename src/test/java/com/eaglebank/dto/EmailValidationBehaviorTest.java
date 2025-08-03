package com.eaglebank.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidationBehaviorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEmptyStringEmailValidation() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("")
                .build();

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        
        System.out.println("Violations for empty string: " + violations.size());
        violations.forEach(v -> System.out.println("Violation: " + v.getMessage()));
    }

    @Test
    void testNullEmailValidation() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email(null)
                .build();

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        
        System.out.println("Violations for null: " + violations.size());
        violations.forEach(v -> System.out.println("Violation: " + v.getMessage()));
    }

    @Test
    void testBlankStringEmailValidation() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("   ")
                .build();

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        
        System.out.println("Violations for blank string: " + violations.size());
        violations.forEach(v -> System.out.println("Violation: " + v.getMessage()));
    }
}
