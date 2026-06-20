package com.creditsimulator.controllers;

import com.creditsimulator.exceptions.ValidationException;
import com.creditsimulator.factories.VehicleFactory;
import com.creditsimulator.models.InstallmentResult;
import com.creditsimulator.models.LoanCalculation;
import com.creditsimulator.models.Vehicle;
import com.creditsimulator.models.VehicleCondition;
import com.creditsimulator.services.ApiService;
import com.creditsimulator.views.ConsoleView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoanController {

    private static final String API_URL_ENV = "CREDIT_API_URL";

    static final double MAX_LOAN_AMOUNT = 1_000_000_000.0;

    static final int MIN_TENOR = 1;
    static final int MAX_TENOR = 6;

    private final ConsoleView view;
    private final ApiService  apiService;

    private final Map<String, LoanCalculation> sheets = new LinkedHashMap<>();

    private String currentSheetName = null;

    public LoanController(ConsoleView view) {
        this.view       = view;
        this.apiService = new ApiService();
    }

    public void run() {
        view.showWelcome();

        boolean running = true;
        while (running) {
            String command = view.promptCommand();
            switch (command.toLowerCase()) {
                case "calculate":
                case "calc":
                    handleCalculate();
                    break;

                case "load":
                    handleLoad();
                    break;

                case "show":
                case "help":
                    view.showHelp();
                    break;

                case "save":
                    handleSave();
                    break;

                case "switch":
                    handleSwitch();
                    break;

                case "list":
                    handleListSheets();
                    break;

                case "exit":
                case "quit":
                case "q":
                    view.showGoodbye();
                    running = false;
                    break;

                case "":
                    // Ignore blank input
                    break;

                default:
                    view.showError("Perintah tidak dikenal: '" + command
                        + "'. Ketik 'show' untuk melihat daftar perintah.");
            }
        }
    }

    private void handleCalculate() {
        try {
            String typeStr = view.prompt("Jenis Kendaraan [Motor/Mobil]");

            String conditionStr = view.prompt("Kondisi Kendaraan [Baru/Bekas]");

            int vehicleYear = view.promptInt("Tahun Kendaraan (4 digit, contoh: 2024)");

            Vehicle vehicle = VehicleFactory.create(typeStr, conditionStr, vehicleYear);

            double loanAmount = view.promptDouble(
                "Jumlah Pinjaman Total (maks Rp 1.000.000.000)");
            validateLoanAmount(loanAmount);

            int tenor = view.promptInt("Tenor Pinjaman [1-6 tahun]");
            validateTenor(tenor);

            double dp = view.promptDouble("Jumlah Down Payment (DP)");
            validateDownPayment(dp, loanAmount, vehicle.getCondition());

            List<InstallmentResult> results = calculate(vehicle, loanAmount, dp, tenor);
            LoanCalculation calc = new LoanCalculation(
                null, vehicle, loanAmount, dp, tenor, results);
            view.displayResults(calc);

            String savePref = view.prompt("Simpan kalkulasi ini sebagai sheet? [y/n]");
            if (savePref.equalsIgnoreCase("y") || savePref.equalsIgnoreCase("yes")) {
                String sheetName = view.prompt("Nama sheet");
                if (sheetName.isBlank()) {
                    view.showError("Nama sheet tidak boleh kosong.");
                } else {
                    saveSheet(sheetName, vehicle, loanAmount, dp, tenor, results);
                }
            }

        } catch (ValidationException e) {
            view.showError("Validasi gagal — " + e.getMessage());
        } catch (NumberFormatException e) {
            view.showError("Input angka tidak valid — " + e.getMessage());
        }
    }

    private void handleLoad() {
        String apiUrl = System.getenv(API_URL_ENV);
        if (apiUrl == null || apiUrl.isBlank()) {
            apiUrl = view.prompt("Masukkan URL API (atau set env " + API_URL_ENV + ")");
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            view.showError("URL API tidak ditemukan. Set environment variable: "
                + API_URL_ENV + "=<url>");
            return;
        }

        view.showInfo("Menghubungi API: " + apiUrl);

        try {
            String json = apiService.get(apiUrl);
            LoanCalculation partial = apiService.parseCalculation(json);

            List<InstallmentResult> results = calculate(
                partial.getVehicle(),
                partial.getLoanAmount(),
                partial.getDownPayment(),
                partial.getTenor()
            );

            LoanCalculation fullCalc = new LoanCalculation(
                partial.getName(),
                partial.getVehicle(),
                partial.getLoanAmount(),
                partial.getDownPayment(),
                partial.getTenor(),
                results
            );

            view.showInfo("Kalkulasi berhasil dimuat dari API.");
            view.displayResults(fullCalc);

            sheets.put(fullCalc.getName(), fullCalc);
            currentSheetName = fullCalc.getName();
            view.showInfo("Disimpan sebagai sheet: '" + fullCalc.getName() + "'.");

        } catch (Exception e) {
            view.showError("Gagal memuat dari API — " + e.getMessage());
        }
    }

    private void handleSave() {
        if (currentSheetName == null || !sheets.containsKey(currentSheetName)) {
            view.showError(
                "Tidak ada kalkulasi aktif. Jalankan 'calculate' atau 'load' terlebih dahulu.");
            return;
        }

        String newName = view.prompt("Nama sheet baru (tekan Enter untuk mempertahankan '"
            + currentSheetName + "')");

        if (newName.isBlank()) {
            newName = currentSheetName;
        }

        LoanCalculation source = sheets.get(currentSheetName);
        LoanCalculation saved = new LoanCalculation(
            newName,
            source.getVehicle(),
            source.getLoanAmount(),
            source.getDownPayment(),
            source.getTenor(),
            source.getResults()
        );

        sheets.put(newName, saved);
        currentSheetName = newName;
        view.showInfo("Sheet disimpan sebagai: '" + newName + "'.");
    }
    private void handleSwitch() {
        if (sheets.isEmpty()) {
            view.showError("Belum ada sheet tersimpan. Jalankan 'calculate' terlebih dahulu.");
            return;
        }

        view.showSheetList(sheets.keySet(), currentSheetName);
        String target = view.prompt("Nama sheet yang ingin ditampilkan");

        if (sheets.containsKey(target)) {
            currentSheetName = target;
            view.showInfo("Berpindah ke sheet: '" + target + "'.");
            view.displayResults(sheets.get(target));
        } else {
            view.showError("Sheet '" + target + "' tidak ditemukan.");
        }
    }

    private void handleListSheets() {
        if (sheets.isEmpty()) {
            view.showInfo("Belum ada sheet tersimpan.");
            return;
        }
        view.showSheetList(sheets.keySet(), currentSheetName);
    }

    public List<InstallmentResult> calculate(
            Vehicle vehicle, double loanAmount, double dp, int maxTenor) {

        double principal = loanAmount - dp;
        double baseRate  = vehicle.getType().getBaseInterestRate();

        List<InstallmentResult> results = new ArrayList<>();
        for (int year = 1; year <= maxTenor; year++) {
            double rate    = calculateInterestRate(baseRate, year);
            // Flat-rate monthly installment
            double monthly = principal * (1.0 + rate * year) / (year * 12.0);
            results.add(new InstallmentResult(year, rate, monthly));
        }

        return results;
    }

    public double calculateInterestRate(double baseRate, int tenorYear) {
        double annualIncrement   = 0.001 * (tenorYear - 1);
        double biennialIncrement = 0.005 * ((tenorYear - 1) / 2);
        return baseRate + annualIncrement + biennialIncrement;
    }

    void validateLoanAmount(double loanAmount) throws ValidationException {
        if (loanAmount <= 0) {
            throw new ValidationException(
                "Jumlah pinjaman harus lebih dari Rp 0.");
        }
        if (loanAmount > MAX_LOAN_AMOUNT) {
            throw new ValidationException(String.format(
                "Jumlah pinjaman tidak boleh melebihi Rp %,.2f. Nilai yang diinput: Rp %,.2f",
                MAX_LOAN_AMOUNT, loanAmount));
        }
    }

    void validateTenor(int tenor) throws ValidationException {
        if (tenor < MIN_TENOR || tenor > MAX_TENOR) {
            throw new ValidationException(String.format(
                "Tenor harus antara %d dan %d tahun. Nilai yang diinput: %d tahun.",
                MIN_TENOR, MAX_TENOR, tenor));
        }
    }

    void validateDownPayment(double dp, double loanAmount, VehicleCondition condition)
            throws ValidationException {
        double minDpPercent = condition.getMinimumDpPercent();
        double minDp        = loanAmount * minDpPercent;

        if (dp < 0) {
            throw new ValidationException("DP tidak boleh negatif.");
        }
        if (dp < minDp) {
            throw new ValidationException(String.format(
                "DP minimum untuk kendaraan %s adalah %.0f%% dari jumlah pinjaman.%n"
                + "  Jumlah pinjaman : Rp %,.2f%n"
                + "  DP minimum      : Rp %,.2f%n"
                + "  DP yang diinput : Rp %,.2f",
                condition.getDisplayName(),
                minDpPercent * 100,
                loanAmount,
                minDp,
                dp));
        }
        if (dp >= loanAmount) {
            throw new ValidationException(
                "DP tidak boleh lebih besar atau sama dengan jumlah pinjaman total.");
        }
    }

    private void saveSheet(String name, Vehicle vehicle, double loanAmount,
                           double dp, int tenor, List<InstallmentResult> results) {
        LoanCalculation calc = new LoanCalculation(name, vehicle, loanAmount, dp, tenor, results);
        sheets.put(name, calc);
        currentSheetName = name;
        view.showInfo("Sheet '" + name + "' berhasil disimpan.");
    }

    Map<String, LoanCalculation> getSheets() {
        return sheets;
    }

    String getCurrentSheetName() {
        return currentSheetName;
    }
}
