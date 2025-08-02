package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import java.time.LocalDateTime;

public class UserResponse {
    
    private String id;
    private String name;
    private Address address;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    // Default constructor
    public UserResponse() {}

    // Constructor with all fields
    public UserResponse(String id, String name, Address address, String phoneNumber, String email, 
                       LocalDateTime createdTimestamp, LocalDateTime updatedTimestamp) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private Address address;
        private String phoneNumber;
        private String email;
        private LocalDateTime createdTimestamp;
        private LocalDateTime updatedTimestamp;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

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

        public Builder createdTimestamp(LocalDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        public Builder updatedTimestamp(LocalDateTime updatedTimestamp) {
            this.updatedTimestamp = updatedTimestamp;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, name, address, phoneNumber, email, createdTimestamp, updatedTimestamp);
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }
} 