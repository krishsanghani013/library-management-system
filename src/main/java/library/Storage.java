package library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class Storage {
    private final Path dataDir = Paths.get("data");
    private final Path booksFile = dataDir.resolve("books.txt");
    private final Path studentsFile = dataDir.resolve("students.txt");
    private final Path issuesFile = dataDir.resolve("issues.txt");

    
    public List<Book> loadBooks() {
        List<Book> catalog = new ArrayList<>();
        if (!Files.exists(booksFile)) {
            System.out.println("[storage] books file missing, starting with an empty catalog.");
            return catalog;
        }

        try {
            List<String> lines = Files.readAllLines(booksFile, StandardCharsets.UTF_8);
            for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
                String row = lines.get(lineNo);
                if (row.isBlank()) {
                    continue;
                }
                String[] parts = row.split("\\|", -1);
                if (parts.length != 5 && parts.length != 6) {
                    System.out.println("[storage][books] skip line " + (lineNo + 1) + " (column mismatch)");
                    continue;
                }
                try {
                    String sectionCode = parts.length == 6 ? sanitize(parts[3]).toUpperCase(Locale.ROOT) : "GEN";
                    int totalUnits = parts.length == 6 ? Integer.parseInt(sanitize(parts[4])) : Integer.parseInt(sanitize(parts[3]));
                    int availableUnits = parts.length == 6 ? Integer.parseInt(sanitize(parts[5])) : Integer.parseInt(sanitize(parts[4]));

                    catalog.add(new Book(
                        Integer.parseInt(sanitize(parts[0])),
                        sanitize(parts[1]),
                        sanitize(parts[2]),
                        sectionCode,
                        totalUnits,
                        availableUnits
                    ));
                } catch (Exception ex) {
                    System.out.println("[storage][books] skip line " + (lineNo + 1) + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load books: " + e.getMessage());
        }
        return catalog;
    }

    
    public List<Student> loadStudents() {
        List<Student> registry = new ArrayList<>();
        if (!Files.exists(studentsFile)) {
            System.out.println("[storage] students file missing, starting with an empty registry.");
            return registry;
        }

        try {
            List<String> lines = Files.readAllLines(studentsFile, StandardCharsets.UTF_8);
            int rowNo = 0;
            for (String row : lines) {
                rowNo++;
                if (row.isBlank()) {
                    continue;
                }
                String[] parts = row.split("\\|", -1);
                if (parts.length != 3 && parts.length != 4) {
                    System.out.println("[storage][students] skip line " + rowNo + " (column mismatch)");
                    continue;
                }

                try {
                    String rawDepartment = parts.length == 4 ? sanitize(parts[3]) : "CSE";
                    String normalizedDepartment = normalizeDepartmentCode(rawDepartment);
                    registry.add(new Student(
                        Integer.parseInt(sanitize(parts[0])),
                        sanitize(parts[1]),
                        sanitize(parts[2]),
                        normalizedDepartment
                    ));
                } catch (Exception ex) {
                    System.out.println("[storage][students] skip line " + rowNo + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load students: " + e.getMessage());
        }
        return registry;
    }

    public List<IssueRecord> loadIssues() {
        List<IssueRecord> circulationLedger = new ArrayList<>();
        if (!Files.exists(issuesFile)) {
            return circulationLedger;
        }
        try {
            List<String> lines = Files.readAllLines(issuesFile, StandardCharsets.UTF_8);
            int lineNumber = 0;
            for (String line : lines) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length != 6) {
                    System.out.println("Skipping malformed issue record at line " + lineNumber + ".");
                    continue;
                }
                try {
                    circulationLedger.add(new IssueRecord(
                        Integer.parseInt(sanitize(parts[0])),
                        Integer.parseInt(sanitize(parts[1])),
                        Integer.parseInt(sanitize(parts[2])),
                        sanitize(parts[3]),
                        sanitize(parts[4]),
                        Boolean.parseBoolean(sanitize(parts[5]))
                    ));
                } catch (Exception parseEx) {
                    System.out.println("Skipping invalid issue record at line " + lineNumber + ": " + parseEx.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load issue records: " + e.getMessage());
        }
        return circulationLedger;
    }

    
    public void saveBooks(List<Book> catalog) {
        List<String> payload = catalog.stream()
            .map(book -> book.getId() + "|" + clean(book.getTitle()) + "|" + clean(book.getAuthor()) + "|" + clean(book.getGenreCode()) + "|" +
                book.getTotalCopies() + "|" + book.getAvailableCopies())
            .collect(Collectors.toList());
        writeFile(booksFile, payload);
    }

    public void saveStudents(List<Student> registry) {
        List<String> payload = new ArrayList<>();
        for (Student member : registry) {
            payload.add(member.getId() + "|" + clean(member.getName()) + "|" + clean(member.getCourse()) + "|" + clean(member.getDepartmentCode()));
        }
        writeFile(studentsFile, payload);
    }

    public void saveIssues(List<IssueRecord> circulationLedger) {
        List<String> payload = new ArrayList<>();
        for (IssueRecord tx : circulationLedger) {
            payload.add(
                tx.getIssueId() + "|" + tx.getBookId() + "|" + tx.getStudentId() + "|" +
                tx.getIssueDate() + "|" + tx.getReturnDate() + "|" + tx.isReturned()
            );
        }
        writeFile(issuesFile, payload);
    }

    private void writeFile(Path file, List<String> lines) {
        try {
            Files.createDirectories(dataDir);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save " + file.getFileName() + ": " + e.getMessage());
        }
    }

    private String clean(String value) {
        return value.replace("|", "/");
    }

    private String sanitize(String value) {
        return value.replace("\uFEFF", "").trim();
    }

    private String normalizeDepartmentCode(String rawDepartment) {
        String code = sanitize(rawDepartment).toUpperCase(Locale.ROOT);
        if ("BSCS".equals(code) || "BSCSH".equals(code)) {
            return "BSCH";
        }
        return code;
    }
}


