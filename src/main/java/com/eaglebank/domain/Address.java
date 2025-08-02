package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    // No @Id needed for embeddable

    @NotBlank(message = "Line1 is required")
    @Column(name = "line1", nullable = false)
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "line3")
    private String line3;

    @NotBlank(message = "Town is required")
    @Column(name = "town", nullable = false)
    private String town;

    @NotBlank(message = "County is required")
    @Column(name = "county", nullable = false)
    private String county;

    @NotBlank(message = "Postcode is required")
    @Pattern(regexp = "^[A-Z]{1,2}[0-9][A-Z0-9]? ?[0-9][A-Z]{2}$", 
             message = "Postcode must be in valid UK format")
    @Column(name = "postcode", nullable = false)
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