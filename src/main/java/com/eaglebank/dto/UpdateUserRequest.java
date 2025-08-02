package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class UpdateUserRequest {
    
    private String name;
    private Address address;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format")
    private String phoneNumber;
    
    @Email(message = "Email must be valid")
    private String email;

    // Default constructor
    public UpdateUserRequest() {}

    // Constructor with all fields
    public UpdateUserRequest(String name, Address address, String phoneNumber, String email) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Address address;
        private String phoneNumber;
        private String email;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(name, address, phoneNumber, email);
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 