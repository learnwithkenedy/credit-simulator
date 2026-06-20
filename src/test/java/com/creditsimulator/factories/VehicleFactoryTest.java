package com.creditsimulator.factories;

import com.creditsimulator.exceptions.ValidationException;
import com.creditsimulator.models.Vehicle;
import com.creditsimulator.models.VehicleCondition;
import com.creditsimulator.models.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VehicleFactory Tests")
class VehicleFactoryTest {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();

    @Test
    @DisplayName("Create Mobil Baru with current year — should succeed")
    void create_MobilBaru_CurrentYear_ReturnsVehicle() throws ValidationException {
        Vehicle v = VehicleFactory.create("Mobil", "Baru", CURRENT_YEAR);
        assertNotNull(v);
        assertEquals(VehicleType.MOBIL, v.getType());
        assertEquals(VehicleCondition.BARU, v.getCondition());
        assertEquals(CURRENT_YEAR, v.getYear());
    }

    @Test
    @DisplayName("Create Motor Baru with currentYear-1 — should succeed (boundary)")
    void create_MotorBaru_CurrentYearMinusOne_ReturnsVehicle() throws ValidationException {
        Vehicle v = VehicleFactory.create("Motor", "Baru", CURRENT_YEAR - 1);
        assertNotNull(v);
        assertEquals(VehicleType.MOTOR, v.getType());
    }

    @Test
    @DisplayName("Create Mobil Bekas with any older year — should succeed")
    void create_MobilBekas_OldYear_ReturnsVehicle() throws ValidationException {
        Vehicle v = VehicleFactory.create("Mobil", "Bekas", 2015);
        assertNotNull(v);
        assertEquals(VehicleCondition.BEKAS, v.getCondition());
    }

    @ParameterizedTest(name = "type=''{0}'' condition=''{1}''")
    @CsvSource({
        "MOBIL, BARU",
        "mobil, baru",
        "Mobil, Baru",
        "MOTOR, BEKAS",
        "motor, bekas",
        "Motor, Bekas"
    })
    @DisplayName("Vehicle creation is case-insensitive for type and condition")
    void create_CaseInsensitiveInputs_ShouldSucceed(String type, String condition)
            throws ValidationException {
        Vehicle v = VehicleFactory.create(type, condition, CURRENT_YEAR - 1);
        assertNotNull(v);
    }

    @Test
    @DisplayName("BARU vehicle with year older than currentYear-1 should throw")
    void create_BaruWithTooOldYear_ThrowsValidationException() {
        int tooOld = CURRENT_YEAR - 5;
        assertThrows(ValidationException.class,
            () -> VehicleFactory.create("Mobil", "Baru", tooOld));
    }

    @Test
    @DisplayName("BEKAS vehicle with old year should NOT throw")
    void create_BekasWithOldYear_ShouldNotThrow() {
        assertDoesNotThrow(() -> VehicleFactory.create("Motor", "Bekas", 2010));
    }

    @ParameterizedTest(name = "invalid type=''{0}''")
    @ValueSource(strings = {"Pesawat", "Kapal", "Truck", "", "  "})
    @DisplayName("Invalid vehicle type strings should throw ValidationException")
    void create_InvalidType_ThrowsValidationException(String invalidType) {
        assertThrows(ValidationException.class,
            () -> VehicleFactory.create(invalidType, "Baru", CURRENT_YEAR));
    }

    @ParameterizedTest(name = "invalid condition=''{0}''")
    @ValueSource(strings = {"Rusak", "Second", "Old", "", "  "})
    @DisplayName("Invalid vehicle condition strings should throw ValidationException")
    void create_InvalidCondition_ThrowsValidationException(String invalidCondition) {
        assertThrows(ValidationException.class,
            () -> VehicleFactory.create("Mobil", invalidCondition, CURRENT_YEAR));
    }

    @Test
    @DisplayName("Year with fewer than 4 digits should throw ValidationException")
    void create_YearWith3Digits_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> VehicleFactory.create("Mobil", "Bekas", 999));
    }

    @Test
    @DisplayName("Year with more than 4 digits should throw ValidationException")
    void create_YearWith5Digits_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> VehicleFactory.create("Mobil", "Bekas", 20250));
    }

    @Test
    @DisplayName("Vehicle toString includes type, condition and year")
    void vehicle_ToString_ContainsAllComponents() throws ValidationException {
        Vehicle v = VehicleFactory.create("Mobil", "Baru", CURRENT_YEAR);
        String str = v.toString();
        assertTrue(str.contains("Mobil"));
        assertTrue(str.contains("Baru"));
        assertTrue(str.contains(String.valueOf(CURRENT_YEAR)));
    }
}
