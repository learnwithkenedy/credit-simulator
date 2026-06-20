package com.creditsimulator.services;

import com.creditsimulator.exceptions.ValidationException;
import com.creditsimulator.factories.VehicleFactory;
import com.creditsimulator.models.LoanCalculation;
import com.creditsimulator.models.Vehicle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class ApiService {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS    = 5000;

    public String get(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        int statusCode = conn.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("API mengembalikan HTTP " + statusCode + " dari " + urlString);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            conn.disconnect();
        }
    }

    public LoanCalculation parseCalculation(String json) throws Exception {
        String vehicleType      = extractStringField(json, "vehicle_type");
        String vehicleCondition = extractStringField(json, "vehicle_condition");
        String yearStr          = extractNumberField(json, "vehicle_year");
        String loanAmountStr    = extractNumberField(json, "loan_amount");
        String downPaymentStr   = extractNumberField(json, "down_payment");
        String tenorStr         = extractNumberField(json, "tenor");
        String name             = extractStringField(json, "name");

        // Validate required fields
        validateRequired(vehicleType,      "vehicle_type");
        validateRequired(vehicleCondition, "vehicle_condition");
        validateRequired(yearStr,          "vehicle_year");
        validateRequired(loanAmountStr,    "loan_amount");
        validateRequired(downPaymentStr,   "down_payment");
        validateRequired(tenorStr,         "tenor");

        int    vehicleYear = Integer.parseInt(yearStr);
        double loanAmount  = Double.parseDouble(loanAmountStr);
        double downPayment = Double.parseDouble(downPaymentStr);
        int    tenor       = Integer.parseInt(tenorStr);

        if (name == null || name.isBlank()) {
            name = "loaded-from-api";
        }

        Vehicle vehicle;
        try {
            vehicle = VehicleFactory.create(vehicleType, vehicleCondition, vehicleYear);
        } catch (ValidationException e) {
            throw new Exception("Data dari API tidak valid: " + e.getMessage(), e);
        }

        return new LoanCalculation(name, vehicle, loanAmount, downPayment, tenor, null);
    }

    private String extractStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return null;

        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx == -1) return null;

        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart == -1) return null;

        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd == -1) return null;

        return json.substring(quoteStart + 1, quoteEnd);
    }

    private String extractNumberField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return null;

        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx == -1) return null;

        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;
        while (end < json.length()
                && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '.'
                    || json.charAt(end) == '-')) {
            end++;
        }

        if (start == end) return null;
        return json.substring(start, end);
    }

    private void validateRequired(String value, String fieldName) throws Exception {
        if (value == null || value.isBlank()) {
            throw new Exception("Field wajib tidak ditemukan di respons API: '" + fieldName + "'");
        }
    }
}
