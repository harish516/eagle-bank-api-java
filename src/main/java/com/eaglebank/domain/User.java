package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "address_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "address_line2")),
        @AttributeOverride(name = "line3", column = @Column(name = "address_line3")),
        @AttributeOverride(name = "town", column = @Column(name = "address_town")),
        @AttributeOverride(name = "county", column = @Column(name = "address_county")),
        @AttributeOverride(name = "postcode", column = @Column(name = "address_postcode"))
    })
    private Address address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone number must be in international format")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreatedDate
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @LastModifiedDate
    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @PrePersist
    @PreUpdate
    public void validate() {
        validateId();
        validatePhoneNumber();
        validateEmail();
    }

    private void validateId() {
        if (id != null && !id.matches("^usr-[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("User ID must match pattern ^usr-[A-Za-z0-9]+$");
        }
    }

    private void validatePhoneNumber() {
        if (phoneNumber != null && !phoneNumber.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException("Phone number must be in international format");
        }
    }

    private void validateEmail() {
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email must be in valid format");
        }
    }
} 