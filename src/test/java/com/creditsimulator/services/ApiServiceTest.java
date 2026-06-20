package com.creditsimulator.services;

import com.creditsimulator.models.LoanCalculation;
import com.creditsimulator.models.VehicleCondition;
import com.creditsimulator.models.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiService Tests")
class ApiServiceTest {

    private ApiService apiService;

    @BeforeEach
    void setUp() {
        apiService = new ApiService();
    }

    @Test
    @DisplayName("Parse well-formed JSON response — Mobil Baru")
    void parseCalculation_WellFormedJson_MobilBaru_ShouldSucceed() throws Exception {
        String json = "{"
            + "\"name\": \"TestLoan\","
            + "\"vehicle_type\": \"Mobil\","
            + "\"vehicle_condition\": \"Baru\","
            + "\"vehicle_year\": 2025,"
            + "\"loan_amount\": 100000000,"
            + "\"down_payment\": 35000000,"
            + "\"tenor\": 3"
            + "}";

        LoanCalculation calc = apiService.parseCalculation(json);

        assertNotNull(calc);
        assertEquals("TestLoan",           calc.getName());
        assertEquals(VehicleType.MOBIL,    calc.getVehicle().getType());
        assertEquals(VehicleCondition.BARU,calc.getVehicle().getCondition());
        assertEquals(2025,                 calc.getVehicle().getYear());
        assertEquals(100_000_000.0,        calc.getLoanAmount(), 0.01);
        assertEquals(35_000_000.0,         calc.getDownPayment(), 0.01);
        assertEquals(3,                    calc.getTenor());
    }

    @Test
    @DisplayName("Parse JSON response — Motor Bekas")
    void parseCalculation_WellFormedJson_MotorBekas_ShouldSucceed() throws Exception {
        String json = "{"
            + "\"name\": \"MotorLama\","
            + "\"vehicle_type\": \"Motor\","
            + "\"vehicle_condition\": \"Bekas\","
            + "\"vehicle_year\": 2018,"
            + "\"loan_amount\": 30000000,"
            + "\"down_payment\": 8000000,"
            + "\"tenor\": 2"
            + "}";

        LoanCalculation calc = apiService.parseCalculation(json);

        assertNotNull(calc);
        assertEquals(VehicleType.MOTOR,     calc.getVehicle().getType());
        assertEquals(VehicleCondition.BEKAS, calc.getVehicle().getCondition());
    }

    @Test
    @DisplayName("Parse JSON with missing name — should default to 'loaded-from-api'")
    void parseCalculation_MissingName_ShouldDefaultName() throws Exception {
        String json = "{"
            + "\"vehicle_type\": \"Mobil\","
            + "\"vehicle_condition\": \"Bekas\","
            + "\"vehicle_year\": 2020,"
            + "\"loan_amount\": 50000000,"
            + "\"down_payment\": 15000000,"
            + "\"tenor\": 1"
            + "}";

        LoanCalculation calc = apiService.parseCalculation(json);
        assertEquals("loaded-from-api", calc.getName());
    }

    @Test
    @DisplayName("Parse JSON with missing required field — should throw Exception")
    void parseCalculation_MissingLoanAmount_ShouldThrowException() {
        String json = "{"
            + "\"vehicle_type\": \"Mobil\","
            + "\"vehicle_condition\": \"Baru\","
            + "\"vehicle_year\": 2025,"
            + "\"down_payment\": 35000000,"
            + "\"tenor\": 3"
            + "}";

        assertThrows(Exception.class, () -> apiService.parseCalculation(json));
    }

    @Test
    @DisplayName("Parse JSON with invalid vehicle type — should throw Exception")
    void parseCalculation_InvalidVehicleType_ShouldThrowException() {
        String json = "{"
            + "\"vehicle_type\": \"Truk\","
            + "\"vehicle_condition\": \"Baru\","
            + "\"vehicle_year\": 2025,"
            + "\"loan_amount\": 100000000,"
            + "\"down_payment\": 35000000,"
            + "\"tenor\": 3"
            + "}";

        assertThrows(Exception.class, () -> apiService.parseCalculation(json));
    }

    @Test
    @DisplayName("Principal is correctly computed from loan - dp")
    void parseCalculation_Principal_IsCorrect() throws Exception {
        String json = "{"
            + "\"vehicle_type\": \"Mobil\","
            + "\"vehicle_condition\": \"Bekas\","
            + "\"vehicle_year\": 2019,"
            + "\"loan_amount\": 80000000,"
            + "\"down_payment\": 20000000,"
            + "\"tenor\": 4"
            + "}";

        LoanCalculation calc = apiService.parseCalculation(json);
        assertEquals(60_000_000.0, calc.getPrincipal(), 0.01);
    }
}
