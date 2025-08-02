package com.eaglebank.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address information")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    // No @Id needed for embeddable

    @NotBlank(message = "Line1 is required")
    @Column(name = "line1", nullable = false)
    @Schema(description = "First line of address", example = "123 Main Street", requiredMode = Schema.RequiredMode.REQUIRED)
    private String line1;

    @Column(name = "line2")
    @Schema(description = "Second line of address", example = "Apartment 4B")
    private String line2;

    @Column(name = "line3")
    @Schema(description = "Third line of address", example = "Building Complex")
    private String line3;

    @NotBlank(message = "Town is required")
    @Column(name = "town", nullable = false)
    @Schema(description = "Town or city", example = "London", requiredMode = Schema.RequiredMode.REQUIRED)
    private String town;

    @NotBlank(message = "County is required")
    @Column(name = "county", nullable = false)
    @Schema(description = "County or state", example = "Greater London", requiredMode = Schema.RequiredMode.REQUIRED)
    private String county;

    @NotBlank(message = "Postcode is required")
    @Pattern(regexp = "^[A-Z]{1,2}[0-9][A-Z0-9]? ?[0-9][A-Z]{2}$", 
             message = "Postcode must be in valid UK format")
    @Column(name = "postcode", nullable = false)
    @Schema(description = "Postal code in UK format", example = "SW1A 1AA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postcode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return line1.equals(address.line1) &&
               town.equals(address.town) &&
               county.equals(address.county) &&
               postcode.equals(address.postcode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(line1, town, county, postcode);
    }
} 