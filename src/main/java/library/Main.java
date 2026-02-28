package library;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Keep one scanner instance for the entire CLI session.
        Scanner scanner = new Scanner(System.in);
        LibraryManager circulationConsole = new LibraryManager(scanner);
        // Manager owns interactive flow; main only wires dependencies.
        circulationConsole.start();
        scanner.close();
    }
}


