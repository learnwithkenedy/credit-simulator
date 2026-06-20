package com.creditsimulator.controllers;

import com.creditsimulator.exceptions.ValidationException;
import com.creditsimulator.models.InstallmentResult;
import com.creditsimulator.models.Vehicle;
import com.creditsimulator.models.VehicleCondition;
import com.creditsimulator.models.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanController Tests")
class LoanControllerTest {

    private LoanController controller;

    @BeforeEach
    void setUp() {
        controller = new LoanController(null);
    }

    @Test
    @DisplayName("Mobil base rate — year 1 should be 8.0%")
    void interestRate_MobilYear1_ShouldBe8Percent() {
        double rate = controller.calculateInterestRate(0.08, 1);
        assertEquals(0.08, rate, 1e-6, "Year 1 = base rate (8%)");
    }

    @Test
    @DisplayName("Mobil year 2 should be 8.1% (+0.1% YoY)")
    void interestRate_MobilYear2_ShouldBe8_1Percent() {
        double rate = controller.calculateInterestRate(0.08, 2);
        assertEquals(0.081, rate, 1e-6, "Year 2 = base + 0.1%");
    }

    @Test
    @DisplayName("Mobil year 3 should be 8.7% (+0.2% YoY + 0.5% biennial)")
    void interestRate_MobilYear3_ShouldBe8_7Percent() {
        double rate = controller.calculateInterestRate(0.08, 3);
        assertEquals(0.087, rate, 1e-6, "Year 3 = base + 0.2% + 0.5%");
    }

    @Test
    @DisplayName("Mobil year 4 should be 8.8%")
    void interestRate_MobilYear4_ShouldBe8_8Percent() {
        double rate = controller.calculateInterestRate(0.08, 4);
        assertEquals(0.088, rate, 1e-6, "Year 4 = base + 0.3% + 0.5%");
    }

    @Test
    @DisplayName("Mobil year 5 should be 9.4%")
    void interestRate_MobilYear5_ShouldBe9_4Percent() {
        double rate = controller.calculateInterestRate(0.08, 5);
        assertEquals(0.094, rate, 1e-6, "Year 5 = base + 0.4% + 1.0%");
    }

    @Test
    @DisplayName("Mobil year 6 should be 9.5%")
    void interestRate_MobilYear6_ShouldBe9_5Percent() {
        double rate = controller.calculateInterestRate(0.08, 6);
        assertEquals(0.095, rate, 1e-6, "Year 6 = base + 0.5% + 1.0%");
    }

    @Test
    @DisplayName("Motor base rate — year 1 should be 9.0%")
    void interestRate_MotorYear1_ShouldBe9Percent() {
        double rate = controller.calculateInterestRate(0.09, 1);
        assertEquals(0.09, rate, 1e-6);
    }

    @ParameterizedTest(name = "year={0}, expected={1}")
    @CsvSource({
        "1, 0.09",
        "2, 0.091",
        "3, 0.097",
        "4, 0.098",
        "5, 0.104",
        "6, 0.105"
    })
    @DisplayName("Motor interest rate per year (parametrized)")
    void interestRate_Motor_ParametrizedYears(int year, double expected) {
        double rate = controller.calculateInterestRate(0.09, year);
        assertEquals(expected, rate, 1e-6,
            "Motor year " + year + " rate should be " + (expected * 100) + "%");
    }

    @Test
    @DisplayName("Calculate returns correct number of results for given tenor")
    void calculate_ReturnsSizeEqualToTenor() {
        Vehicle vehicle = new Vehicle(VehicleType.MOBIL, VehicleCondition.BARU, 2025);
        List<InstallmentResult> results = controller.calculate(vehicle, 100_000_000, 35_000_000, 3);
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Year 1 monthly installment — Mobil, 100M loan, 35M DP, 1-yr tenor")
    void calculate_Year1MonthlyInstallment_IsCorrect() {
        Vehicle vehicle = new Vehicle(VehicleType.MOBIL, VehicleCondition.BARU, 2025);
        List<InstallmentResult> results = controller.calculate(vehicle, 100_000_000, 35_000_000, 1);
        assertEquals(5_850_000.0, results.get(0).getMonthlyInstallment(), 1.0);
    }

    @Test
    @DisplayName("Monthly installment decreases as tenor increases (principal paid over more months)")
    void calculate_MonthlyInstallmentDecreasesWithLongerTenor() {
        Vehicle vehicle = new Vehicle(VehicleType.MOBIL, VehicleCondition.BARU, 2025);
        List<InstallmentResult> results = controller.calculate(vehicle, 100_000_000, 35_000_000, 6);
        double year1Monthly = results.get(0).getMonthlyInstallment();
        double year6Monthly = results.get(5).getMonthlyInstallment();
        assertTrue(year1Monthly > year6Monthly,
            "Year-1 monthly installment should be higher than year-6");
    }

    @Test
    @DisplayName("Correct interest rate stored in result")
    void calculate_StoresCorrectInterestRateInResult() {
        Vehicle vehicle = new Vehicle(VehicleType.MOTOR, VehicleCondition.BEKAS, 2020);
        List<InstallmentResult> results = controller.calculate(vehicle, 50_000_000, 15_000_000, 2);
        assertEquals(0.09,  results.get(0).getInterestRate(), 1e-6, "Year 1 Motor rate = 9%");
        assertEquals(0.091, results.get(1).getInterestRate(), 1e-6, "Year 2 Motor rate = 9.1%");
    }

    @Test
    @DisplayName("Loan amount > 1 billion should throw ValidationException")
    void validateLoanAmount_AboveMax_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> controller.validateLoanAmount(1_000_000_001.0));
    }

    @Test
    @DisplayName("Loan amount of 0 should throw ValidationException")
    void validateLoanAmount_Zero_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> controller.validateLoanAmount(0));
    }

    @Test
    @DisplayName("Loan amount of 1 billion is exactly at maximum — should pass")
    void validateLoanAmount_ExactlyAtMax_ShouldPass() {
        assertDoesNotThrow(() -> controller.validateLoanAmount(1_000_000_000.0));
    }

    @Test
    @DisplayName("Tenor of 0 should throw ValidationException")
    void validateTenor_Zero_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> controller.validateTenor(0));
    }

    @Test
    @DisplayName("Tenor of 7 should throw ValidationException")
    void validateTenor_Seven_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> controller.validateTenor(7));
    }

    @Test
    @DisplayName("Tenor of 6 is at maximum — should pass")
    void validateTenor_Six_ShouldPass() {
        assertDoesNotThrow(() -> controller.validateTenor(6));
    }

    @Test
    @DisplayName("DP < 35% for BARU should throw ValidationException")
    void validateDownPayment_Baru_BelowMinimum_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> controller.validateDownPayment(30_000_000, 100_000_000, VehicleCondition.BARU));
    }

    @Test
    @DisplayName("DP = 35% for BARU should pass")
    void validateDownPayment_Baru_ExactlyMinimum_ShouldPass() {
        assertDoesNotThrow(
            () -> controller.validateDownPayment(35_000_000, 100_000_000, VehicleCondition.BARU));
    }

    @Test
    @DisplayName("DP < 25% for BEKAS should throw ValidationException")
    void validateDownPayment_Bekas_BelowMinimum_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> controller.validateDownPayment(20_000_000, 100_000_000, VehicleCondition.BEKAS));
    }

    @Test
    @DisplayName("DP = 25% for BEKAS should pass")
    void validateDownPayment_Bekas_ExactlyMinimum_ShouldPass() {
        assertDoesNotThrow(
            () -> controller.validateDownPayment(25_000_000, 100_000_000, VehicleCondition.BEKAS));
    }

    @Test
    @DisplayName("DP equal to loan amount should throw ValidationException")
    void validateDownPayment_EqualToLoanAmount_ThrowsValidationException() {
        assertThrows(ValidationException.class,
            () -> controller.validateDownPayment(100_000_000, 100_000_000, VehicleCondition.BEKAS));
    }
}
