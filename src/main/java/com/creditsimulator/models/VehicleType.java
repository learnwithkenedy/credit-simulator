package com.creditsimulator.models;

public enum VehicleType {

    MOTOR("Motor", 0.09),
    MOBIL("Mobil", 0.08);

    private final String displayName;
    private final double baseInterestRate;

    VehicleType(String displayName, double baseInterestRate) {
        this.displayName = displayName;
        this.baseInterestRate = baseInterestRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseInterestRate() {
        return baseInterestRate;
    }

    public static VehicleType fromString(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Jenis kendaraan tidak boleh kosong.");
        }
        for (VehicleType type : values()) {
            if (type.name().equalsIgnoreCase(s.trim()) ||
                type.displayName.equalsIgnoreCase(s.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            "Jenis kendaraan tidak valid: '" + s + "'. Gunakan 'Motor' atau 'Mobil'."
        );
    }
}
