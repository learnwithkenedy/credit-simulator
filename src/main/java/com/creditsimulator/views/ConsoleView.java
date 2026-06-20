package com.creditsimulator.views;

import com.creditsimulator.models.InstallmentResult;
import com.creditsimulator.models.LoanCalculation;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

public class ConsoleView {

    private static final String BORDER  = "═══════════════════════════════════════════════════════";
    private static final String DIVIDER = "───────────────────────────────────────────────────────";
    private static final Locale ID_LOCALE = new Locale("id", "ID");

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void showWelcome() {
        out.println();
        out.println("  ╔" + repeat("═", 55) + "╗");
        out.println("  ║          CREDIT SIMULATOR  v1.0.0                     ║");
        out.println("  ║   Simulasi Cicilan Kredit Kendaraan Bermotor           ║");
        out.println("  ╚" + repeat("═", 55) + "╝");
        out.println();
        out.println("  Ketik 'show' untuk melihat semua perintah yang tersedia.");
        out.println();
    }

    public void showGoodbye() {
        out.println();
        out.println("  Terima kasih telah menggunakan Credit Simulator.");
        out.println("  Sampai jumpa! 👋");
        out.println();
    }

    public void showHelp() {
        out.println();
        out.println("  ╔" + repeat("═", 55) + "╗");
        out.println("  ║                 DAFTAR PERINTAH                        ║");
        out.println("  ╠" + repeat("═", 55) + "╣");
        out.println("  ║  calculate  — Hitung cicilan pinjaman kendaraan baru   ║");
        out.println("  ║  load       — Muat kalkulasi dari REST API             ║");
        out.println("  ║  save       — Simpan kalkulasi aktif ke dalam sheet    ║");
        out.println("  ║  switch     — Pindah ke sheet kalkulasi lain           ║");
        out.println("  ║  list       — Tampilkan semua sheet tersimpan          ║");
        out.println("  ║  show       — Tampilkan daftar perintah ini            ║");
        out.println("  ║  exit       — Keluar dari aplikasi                     ║");
        out.println("  ╚" + repeat("═", 55) + "╝");
        out.println();
    }

    public String promptCommand() {
        out.print("  credit-simulator> ");
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "exit";
    }

    public String prompt(String label) {
        out.print("    " + label + ": ");
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            return line;
        }
        return "";
    }

    public int promptInt(String label) {
        String raw = prompt(label);
        String digits = raw.replaceAll("[^0-9\\-]", "");
        if (digits.isEmpty()) {
            throw new NumberFormatException("Input tidak mengandung angka: '" + raw + "'");
        }
        return Integer.parseInt(digits);
    }

    public double promptDouble(String label) {
        String raw = prompt(label);
        String cleaned = raw
                .replaceAll("[Rr][Pp]\\.?\\s*", "")  
                .replaceAll("[^0-9.]", "")            
                .trim();
        if (cleaned.isEmpty()) {
            throw new NumberFormatException("Input tidak mengandung angka: '" + raw + "'");
        }
        return Double.parseDouble(cleaned);
    }

    public void displayResults(LoanCalculation calc) {
        NumberFormat currFmt = NumberFormat.getCurrencyInstance(ID_LOCALE);
        currFmt.setMaximumFractionDigits(2);
        currFmt.setMinimumFractionDigits(2);

        out.println();
        out.println("  " + BORDER);
        String title = "  HASIL KALKULASI KREDIT"
                + (calc.getName() != null && !calc.getName().isBlank()
                   ? " — " + calc.getName() : "");
        out.println("  " + title);
        out.println("  " + BORDER);
        out.printf("  Kendaraan      : %s%n", calc.getVehicle());
        out.printf("  Pinjaman Total : Rp %,.2f%n", calc.getLoanAmount());
        out.printf("  Down Payment   : Rp %,.2f%n", calc.getDownPayment());
        out.printf("  Pokok Kredit   : Rp %,.2f%n", calc.getPrincipal());
        out.println("  " + DIVIDER);
        out.println("  Rincian Cicilan per Pilihan Tenor:");
        out.println();

        for (InstallmentResult r : calc.getResults()) {
            out.printf("  Tahun %-2d : Rp %,.2f/bln  ,  Suku Bunga : %.1f%%%n",
                r.getTenorYear(),
                r.getMonthlyInstallment(),
                r.getInterestRate() * 100.0);
        }

        out.println("  " + BORDER);
        out.println();
    }

    public void showSheetList(Set<String> sheetNames, String currentSheet) {
        out.println();
        out.println("  Sheet Tersimpan:");
        int i = 1;
        for (String name : sheetNames) {
            String marker = name.equals(currentSheet) ? "  ◀ aktif" : "";
            out.printf("    %d. %s%s%n", i++, name, marker);
        }
        out.println();
    }

    public void showError(String message) {
        out.println();
        out.println("  ✖ ERROR: " + message);
        out.println();
    }

    public void showInfo(String message) {
        out.println("  ✔ " + message);
    }

    public void showWarning(String message) {
        out.println("  ⚠ PERINGATAN: " + message);
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
