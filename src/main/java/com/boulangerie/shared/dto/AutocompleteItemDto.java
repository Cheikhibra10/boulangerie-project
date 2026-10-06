package com.boulangerie.shared.dto;

public record AutocompleteItemDto(
        Long id,
        String label,
        String subtitle
) {
    public static AutocompleteItemDto of(Long id, String label) {
        return new AutocompleteItemDto(id, label, null);
    }

    public static AutocompleteItemDto of(Long id, String label, String subtitle) {
        return new AutocompleteItemDto(id, label, subtitle);
    }
}