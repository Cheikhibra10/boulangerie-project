package com.boulangerie.shared.dto;

public record UserSummary(

        String id,

        String username,

        String firstName,

        String lastName
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}