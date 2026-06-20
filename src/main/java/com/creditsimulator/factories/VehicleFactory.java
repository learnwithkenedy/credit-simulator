package com.creditsimulator.factories;

import com.creditsimulator.exceptions.ValidationException;
import com.creditsimulator.models.Vehicle;
import com.creditsimulator.models.VehicleCondition;
import com.creditsimulator.models.VehicleType;

import java.time.LocalDate;

public class VehicleFactory {

    public static Vehicle create(String typeStr, String conditionStr, int year) throws ValidationException {
        VehicleType type;
        try {
            type = VehicleType.fromString(typeStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }

        VehicleCondition condition;
        try {
            condition = VehicleCondition.fromString(conditionStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }

        if (year < 1000 || year > 9999) {
            throw new ValidationException(
                "Tahun kendaraan harus berupa 4 digit angka. Contoh: 2023"
            );
        }

        if (condition == VehicleCondition.BARU) {
            int currentYear = LocalDate.now().getYear();
            int minimumYear = currentYear - 1;
            if (year < minimumYear) {
                throw new ValidationException(String.format(
                    "Kendaraan BARU harus memiliki tahun >= %d. Tahun yang diinput: %d.%n"
                    + "Untuk kendaraan tahun %d, gunakan kondisi 'Bekas'.",
                    minimumYear, year, year
                ));
            }
        }

        return new Vehicle(type, condition, year);
    }
}
