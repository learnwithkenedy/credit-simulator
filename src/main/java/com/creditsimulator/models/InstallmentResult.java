package com.creditsimulator.models;

public class InstallmentResult {

    private final int tenorYear;
    private final double interestRate;
    private final double monthlyInstallment;

    public InstallmentResult(int tenorYear, double interestRate, double monthlyInstallment) {
        this.tenorYear = tenorYear;
        this.interestRate = interestRate;
        this.monthlyInstallment = monthlyInstallment;
    }

    public int getTenorYear() {
        return tenorYear;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getMonthlyInstallment() {
        return monthlyInstallment;
    }
}
