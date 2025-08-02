package com.eaglebank.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

class UserTest {

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        user = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateUserWithValidData() {
        assertThat(user.getId()).isEqualTo("usr-abc123");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getAddress()).isEqualTo(address);
        assertThat(user.getPhoneNumber()).isEqualTo("+44123456789");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldValidateUserIdFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("invalid-id")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must match pattern");
    }

    @Test
    void shouldValidatePhoneNumberFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("invalid-phone")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }

    @Test
    void shouldValidateEmailFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("invalid-email")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldUpdateUserDetails() {
        Address newAddress = Address.builder()
                .line1("456 New Street")
                .town("Manchester")
                .county("Greater Manchester")
                .postcode("M1 1AA")
                .build();

        user.setName("Updated User");
        user.setAddress(newAddress);
        user.setPhoneNumber("+44987654321");
        user.setEmail("updated@example.com");

        assertThat(user.getName()).isEqualTo("Updated User");
        assertThat(user.getAddress()).isEqualTo(newAddress);
        assertThat(user.getPhoneNumber()).isEqualTo("+44987654321");
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
    }
} 