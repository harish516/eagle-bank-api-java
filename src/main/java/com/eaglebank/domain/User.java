package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "User ID is required")
    @Size(max = 255, message = "User ID exceeds maximum length")
    @Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "User ID must match pattern ^usr-[A-Za-z0-9]+$")
    @Column(name = "id", nullable = false)
    private String id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name exceeds maximum length")
    @Column(name = "name", nullable = false)
    private String name;

    @Valid
    @NotNull(message = "Address is required")
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
    @Size(max = 16, message = "Phone number exceeds maximum length")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone number must be in international format")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email exceeds maximum length")
    @Email(message = "Email must be in valid format")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreatedDate
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @LastModifiedDate
    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    // Custom constructor for Lombok Builder compatibility
    public User(String id, String name, Address address, String phoneNumber, String email, 
                LocalDateTime createdTimestamp, LocalDateTime updatedTimestamp) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
        // Bean validation will be triggered by JPA lifecycle events and @Valid annotations
    }
} 