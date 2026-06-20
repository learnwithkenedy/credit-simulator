package com.creditsimulator;

import com.creditsimulator.controllers.LoanController;
import com.creditsimulator.views.ConsoleView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        PrintStream outputStream = System.out;

        if (args.length > 0) {
            String filePath = args[0];
            try {
                inputStream = new FileInputStream(filePath);
                System.out.println("[INFO] Membaca input dari file: " + filePath);
            } catch (Exception e) {
                System.err.println("[ERROR] File tidak ditemukan: " + filePath);
                System.exit(1);
            }
        }

        ConsoleView view = new ConsoleView(inputStream, outputStream);
        LoanController controller = new LoanController(view);
        controller.run();
    }
}
