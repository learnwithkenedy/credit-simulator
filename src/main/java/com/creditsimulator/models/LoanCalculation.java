package com.creditsimulator.models;

import java.util.Collections;
import java.util.List;

public class LoanCalculation {

    private final String name;
    private final Vehicle vehicle;
    private final double loanAmount;
    private final double downPayment;
    private final int tenor;
    private final List<InstallmentResult> results;

    public LoanCalculation(
            String name,
            Vehicle vehicle,
            double loanAmount,
            double downPayment,
            int tenor,
            List<InstallmentResult> results) {
        this.name = name;
        this.vehicle = vehicle;
        this.loanAmount = loanAmount;
        this.downPayment = downPayment;
        this.tenor = tenor;
        this.results = results != null ? Collections.unmodifiableList(results) : null;
    }

    public String getName() {
        return name;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getDownPayment() {
        return downPayment;
    }

    public double getPrincipal() {
        return loanAmount - downPayment;
    }

    public int getTenor() {
        return tenor;
    }

    public List<InstallmentResult> getResults() {
        return results;
    }
}
