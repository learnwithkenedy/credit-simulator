package com.creditsimulator.models;

public enum VehicleCondition {

    BARU("Baru", 0.35),
    BEKAS("Bekas", 0.25);

    private final String displayName;
    private final double minimumDpPercent;

    VehicleCondition(String displayName, double minimumDpPercent) {
        this.displayName = displayName;
        this.minimumDpPercent = minimumDpPercent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinimumDpPercent() {
        return minimumDpPercent;
    }

    public static VehicleCondition fromString(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Kondisi kendaraan tidak boleh kosong.");
        }
        for (VehicleCondition condition : values()) {
            if (condition.name().equalsIgnoreCase(s.trim()) ||
                condition.displayName.equalsIgnoreCase(s.trim())) {
                return condition;
            }
        }
        throw new IllegalArgumentException(
            "Kondisi kendaraan tidak valid: '" + s + "'. Gunakan 'Baru' atau 'Bekas'."
        );
    }
}
