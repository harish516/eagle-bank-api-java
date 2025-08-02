package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

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

    // Custom constructor that calls validation
    public User(String id, String name, Address address, String phoneNumber, String email, 
                LocalDateTime createdTimestamp, LocalDateTime updatedTimestamp) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
        validate();
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        validateId();
        validateName();
        validatePhoneNumber();
        validateEmail();
        validateAddress();
    }

    private void validateId() {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (id.length() > 255) {
            throw new IllegalArgumentException("User ID exceeds maximum length");
        }
        if (!id.matches("^usr-[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("User ID must match pattern ^usr-[A-Za-z0-9]+$");
        }
    }

    private void validateName() {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Name exceeds maximum length");
        }
    }

    private void validatePhoneNumber() {
        if (phoneNumber == null) {
            throw new NullPointerException("phoneNumber cannot be null");
        }
        if (phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (phoneNumber.length() > 16) {
            throw new IllegalArgumentException("Phone number exceeds maximum length");
        }
        if (!phoneNumber.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException("Phone number must be in international format");
        }
    }

    private void validateEmail() {
        if (email == null) {
            throw new NullPointerException("email cannot be null");
        }
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email exceeds maximum length");
        }
        // More lenient email regex that allows common valid formats
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") || 
            email.startsWith(".") || email.contains("..") || email.endsWith(".@") ||
            email.split("@")[0].endsWith(".")) {
            throw new IllegalArgumentException("Email must be in valid format");
        }
    }

    private void validateAddress() {
        if (address == null) {
            throw new NullPointerException("address cannot be null");
        }
    }

    // Override setters to add validation
    public void setId(String id) {
        this.id = id;
        validateId();
    }

    public void setName(String name) {
        this.name = name;
        validateName();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        validatePhoneNumber();
    }

    public void setEmail(String email) {
        this.email = email;
        validateEmail();
    }

    public void setAddress(Address address) {
        this.address = address;
        validateAddress();
    }
} 