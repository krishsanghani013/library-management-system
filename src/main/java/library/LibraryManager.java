package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;


public class LibraryManager {
    private static final int MAX_ACTIVE_LOANS_PER_STUDENT = 3;
    private static final int STANDARD_LOAN_WINDOW_DAYS = 14;

    private static final Map<String, String> BOOK_GENRES = createBookGenres();
    private static final Map<String, String> TECH_DEPARTMENTS = createTechDepartments();

    private final List<Book> catalogTitles;
    private final List<Student> studentRegistry;
    private final List<IssueRecord> circulationLedger;
    private final Storage dataStore;
    private final Scanner consoleIn;

    private int nextCatalogId;
    private int nextStudentId;
    private int nextTransactionId;

    public LibraryManager(Scanner consoleIn) {
        this.consoleIn = consoleIn;
        this.dataStore = new Storage();
        this.catalogTitles = dataStore.loadBooks();
        this.studentRegistry = dataStore.loadStudents();
        this.circulationLedger = dataStore.loadIssues();

        this.nextCatalogId = getNextBookId();
        this.nextStudentId = getNextStudentId();
        this.nextTransactionId = getNextIssueId();
    }

    public void start() {
        boolean shouldContinue = true;
        while (shouldContinue) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> showAllBooks();
                case 3 -> registerStudent();
                case 4 -> showAllStudents();
                case 5 -> issueBook();
                case 6 -> returnBook();
                case 7 -> showIssueRecords();
                case 8 -> searchBookByTitle();
                case 9 -> {
                    saveData();
                    shouldContinue = false;
                    System.out.println("Data saved. Bye.");
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== Library Management System =====");
        System.out.println("1. Add Book");
        System.out.println("2. View All Books");
        System.out.println("3. Register Student");
        System.out.println("4. View All Students");
        System.out.println("5. Issue Book");
        System.out.println("6. Return Book");
        System.out.println("7. View Issue Records");
        System.out.println("8. Search Book by Title");
        System.out.println("9. Save and Exit");
    }

    private void addBook() {
        System.out.println();
        System.out.println("---- Add Book ----");
        String titleText = readText("Enter book title: ");
        String authorName = readText("Enter author name: ");
        String sectionCode = promptForBookGenreCode();
        int inboundCopies = readInt("Enter number of copies: ");

        if (inboundCopies <= 0) {
            System.out.println("Copies should be greater than 0.");
            return;
        }

        Book existingTitle = findBookByTitleAuthorAndGenre(titleText, authorName, sectionCode);
        if (existingTitle != null) {
            existingTitle.increaseCopies(inboundCopies);
            System.out.println("Book already existed in section " + sectionCode + ". Copies updated.");
            return;
        }

        Book newCatalogEntry = new Book(nextCatalogId++, titleText, authorName, sectionCode, inboundCopies, inboundCopies);
        catalogTitles.add(newCatalogEntry);
        System.out.println("Book added with ID: " + newCatalogEntry.getId() + " in section " + sectionCode + ".");
    }

    private void showAllBooks() {
        System.out.println();
        System.out.println("---- Book List ----");
        if (catalogTitles.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        for (Map.Entry<String, String> genre : BOOK_GENRES.entrySet()) {
            String sectionCode = genre.getKey();
            List<Book> sectionBooks = new ArrayList<>();
            for (Book catalogEntry : catalogTitles) {
                if (sectionCode.equalsIgnoreCase(catalogEntry.getGenreCode())) {
                    sectionBooks.add(catalogEntry);
                }
            }
            if (sectionBooks.isEmpty()) {
                continue;
            }

            System.out.println("[" + sectionCode + "] " + genre.getValue());
            for (Book catalogEntry : sectionBooks) {
                System.out.println("ID: " + catalogEntry.getId() +
                    ", Title: " + catalogEntry.getTitle() +
                    ", Author: " + catalogEntry.getAuthor() +
                    ", Section: " + catalogEntry.getGenreCode() +
                    ", Available: " + catalogEntry.getAvailableCopies() + "/" + catalogEntry.getTotalCopies());
            }
        }

        List<Book> otherBooks = new ArrayList<>();
        for (Book catalogEntry : catalogTitles) {
            if (!BOOK_GENRES.containsKey(catalogEntry.getGenreCode().toUpperCase(Locale.ROOT))) {
                otherBooks.add(catalogEntry);
            }
        }
        if (!otherBooks.isEmpty()) {
            System.out.println("[GEN] General");
            otherBooks.forEach(catalogEntry ->
                System.out.println("ID: " + catalogEntry.getId() +
                    ", Title: " + catalogEntry.getTitle() +
                    ", Author: " + catalogEntry.getAuthor() +
                    ", Section: " + catalogEntry.getGenreCode() +
                    ", Available: " + catalogEntry.getAvailableCopies() + "/" + catalogEntry.getTotalCopies())
            );
        }
    }

    private void registerStudent() {
        System.out.println();
        System.out.println("---- Register Student ----");
        String displayName = normalizePersonName(readText("Enter student name: "));
        String departmentCode = promptForDepartmentCode();
        String programLabel = TECH_DEPARTMENTS.get(departmentCode);

        Student profile = new Student(nextStudentId++, displayName, programLabel, departmentCode);
        studentRegistry.add(profile);
        System.out.println("Student registered with ID: " + profile.getId() + " in department " + departmentCode + ".");
    }

    private void showAllStudents() {
        System.out.println();
        System.out.println("---- Student List ----");
        if (studentRegistry.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        for (Map.Entry<String, String> department : TECH_DEPARTMENTS.entrySet()) {
            String departmentCode = department.getKey();
            List<Student> sectionStudents = studentRegistry.stream()
                .filter(student -> departmentCode.equalsIgnoreCase(student.getDepartmentCode()))
                .collect(Collectors.toList());
            if (sectionStudents.isEmpty()) {
                continue;
            }

            System.out.println("[" + departmentCode + "] " + department.getValue());
            for (Student member : sectionStudents) {
                System.out.println("ID: " + member.getId() +
                    ", Name: " + member.getName() +
                    ", Course: " + member.getCourse() +
                    ", Department: " + member.getDepartmentCode());
            }
        }

        List<Student> unmappedStudents = new ArrayList<>();
        for (Student student : studentRegistry) {
            if (!TECH_DEPARTMENTS.containsKey(student.getDepartmentCode().toUpperCase(Locale.ROOT))) {
                unmappedStudents.add(student);
            }
        }
        if (!unmappedStudents.isEmpty()) {
            System.out.println("[GEN] General / Legacy");
            for (Student student : unmappedStudents) {
                System.out.println("ID: " + student.getId() +
                    ", Name: " + student.getName() +
                    ", Course: " + student.getCourse() +
                    ", Department: " + student.getDepartmentCode());
            }
        }
    }

    private void issueBook() {
        System.out.println();
        System.out.println("---- Issue Book ----");
        int catalogId = readInt("Enter book ID: ");
        int borrowerId = readInt("Enter student ID: ");

        Book title = findBookById(catalogId);
        if (title == null) {
            System.out.println("Book not found.");
            return;
        }

        Student borrower = findStudentById(borrowerId);
        if (borrower == null) {
            System.out.println("Student not found.");
            return;
        }

        if (!title.issueOneCopy()) {
            System.out.println("Book is not available.");
            return;
        }

        try {
            guardLoanPolicy(catalogId, borrowerId);
            IssueRecord loanTransaction = new IssueRecord(nextTransactionId++, catalogId, borrowerId, LocalDate.now());
            circulationLedger.add(loanTransaction);
            System.out.println("Book issued successfully. Issue ID: " + loanTransaction.getIssueId());
        } catch (CirculationPolicyException policyError) {
            title.returnOneCopy();
            System.out.println("Issue blocked: " + policyError.getMessage());
            System.err.println("[circulation][warn] issue rejected for student " + borrowerId + ": " + policyError.getMessage());
        }
    }

    private void returnBook() {
        System.out.println();
        System.out.println("---- Return Book ----");
        int transactionId = readInt("Enter issue ID: ");

        IssueRecord loanTransaction = findIssueById(transactionId);
        if (loanTransaction == null) {
            System.out.println("Issue record not found.");
            return;
        }

        if (loanTransaction.isReturned()) {
            System.out.println("Book is already returned.");
            return;
        }

        Book title = findBookById(loanTransaction.getBookId());
        if (title == null) {
            System.out.println("Book not found for this issue record.");
            return;
        }

        if (!title.returnOneCopy()) {
            System.err.println("[circulation][warn] attempted return for book " + title.getId() + " but stock is already full.");
        }

        try {
            loanTransaction.markReturned(LocalDate.now());
            System.out.println("Book returned successfully.");
        } catch (CirculationPolicyException policyError) {
            System.out.println("Return failed: " + policyError.getMessage());
        }
    }

    private void showIssueRecords() {
        System.out.println();
        System.out.println("---- Issue Records ----");
        if (circulationLedger.isEmpty()) {
            System.out.println("No issue records found.");
            return;
        }

        for (IssueRecord tx : circulationLedger) {
            Book book = findBookById(tx.getBookId());
            Student borrower = findStudentById(tx.getStudentId());
            String bookName = book != null ? book.getTitle() : "Unknown Book";
            String borrowerName = borrower != null ? borrower.getName() : "Unknown Student";
            boolean overdue = tx.isOverdue(STANDARD_LOAN_WINDOW_DAYS, LocalDate.now());
            String status = tx.isReturned() ? "Returned on " + tx.getReturnDate() : "Not Returned";

            System.out.println(
                "Issue ID: " + tx.getIssueId() +
                ", Book: " + bookName +
                ", Student: " + borrowerName +
                ", Issued: " + tx.getIssueDate() +
                ", Status: " + status +
                (overdue && !tx.isReturned() ? ", Flag: OVERDUE" : "")
            );
        }
    }

    private void searchBookByTitle() {
        System.out.println();
        System.out.println("---- Search Book ----");
        String query = readText("Enter title keyword: ").toLowerCase(Locale.ROOT);
        List<Book> hits = catalogTitles.stream()
            .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(query))
            .collect(Collectors.toList());

        if (hits.isEmpty()) {
            System.out.println("No books matched your search.");
            return;
        }

        for (Book title : hits) {
            System.out.println("ID: " + title.getId() +
                ", Title: " + title.getTitle() +
                ", Author: " + title.getAuthor() +
                ", Section: " + title.getGenreCode() +
                ", Available: " + title.getAvailableCopies() + "/" + title.getTotalCopies());
        }
    }

    private Book findBookById(int id) {
        for (Book title : catalogTitles) {
            if (title.getId() == id) {
                return title;
            }
        }
        return null;
    }

    private Book findBookByTitleAuthorAndGenre(String title, String author, String genreCode) {
        for (Book candidate : catalogTitles) {
            if (candidate.getTitle().equalsIgnoreCase(title)
                && candidate.getAuthor().equalsIgnoreCase(author)
                && candidate.getGenreCode().equalsIgnoreCase(genreCode)) {
                return candidate;
            }
        }
        return null;
    }

    private Student findStudentById(int id) {
        for (Student member : studentRegistry) {
            if (member.getId() == id) {
                return member;
            }
        }
        return null;
    }

    private IssueRecord findIssueById(int id) {
        for (IssueRecord transaction : circulationLedger) {
            if (transaction.getIssueId() == id) {
                return transaction;
            }
        }
        return null;
    }

    private int getNextBookId() {
        int max = 0;
        for (Book title : catalogTitles) {
            if (title.getId() > max) {
                max = title.getId();
            }
        }
        return max + 1;
    }

    private int getNextStudentId() {
        int max = 0;
        for (Student member : studentRegistry) {
            if (member.getId() > max) {
                max = member.getId();
            }
        }
        return max + 1;
    }

    private int getNextIssueId() {
        int max = 0;
        for (IssueRecord transaction : circulationLedger) {
            if (transaction.getIssueId() > max) {
                max = transaction.getIssueId();
            }
        }
        return max + 1;
    }

    private int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = consoleIn.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readText(String message) {
        while (true) {
            System.out.print(message);
            String value = consoleIn.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Value cannot be empty.");
        }
    }

    private String promptForBookGenreCode() {
        System.out.println("Select book section (genre):");
        for (Map.Entry<String, String> entry : BOOK_GENRES.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        while (true) {
            String code = readText("Enter section code: ").toUpperCase(Locale.ROOT);
            if (BOOK_GENRES.containsKey(code)) {
                return code;
            }
            System.out.println("Invalid code. Please choose from the listed genre codes.");
        }
    }

    private String promptForDepartmentCode() {
        System.out.println("Select student department (tech only):");
        for (Map.Entry<String, String> entry : TECH_DEPARTMENTS.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        while (true) {
            String code = readText("Enter department code: ").toUpperCase(Locale.ROOT);
            if (TECH_DEPARTMENTS.containsKey(code)) {
                return code;
            }
            System.out.println("Invalid code. Please choose from the listed department codes.");
        }
    }

    private static Map<String, String> createBookGenres() {
        Map<String, String> genres = new LinkedHashMap<>();
        genres.put("FIC", "Fiction");
        genres.put("SCI", "Science");
        genres.put("TEC", "Technology");
        genres.put("MAT", "Mathematics");
        genres.put("HIS", "History");
        genres.put("BIO", "Biography");
        genres.put("ART", "Arts");
        genres.put("LIT", "Literature");
        return genres;
    }

    private static Map<String, String> createTechDepartments() {
        Map<String, String> departments = new LinkedHashMap<>();
        departments.put("CSE", "B.Tech. (Computer Science and Engineering)");
        departments.put("CSEAIML", "B.Tech. (CSE - AI & ML)");
        departments.put("CIV", "B.Tech. (Civil Engineering)");
        departments.put("MEC", "B.Tech. (Mechanical Engineering)");
        departments.put("BSCH", "B.Sc. (Honors)");
        departments.put("DCE", "Diploma (Computer Engineering)");
        departments.put("DCIV", "Diploma (Civil Engineering)");
        departments.put("DEE", "Diploma (Electrical Engineering)");
        departments.put("DMEC", "Diploma (Mechanical Engineering)");
        return departments;
    }

    private String normalizePersonName(String rawName) {
        String cleaned = rawName.trim().replaceAll("\\s+", " ");
        String[] tokens = cleaned.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String part = tokens[i];
            if (part.isEmpty()) {
                continue;
            }
            String normalized = part.substring(0, 1).toUpperCase(Locale.ROOT) +
                part.substring(1).toLowerCase(Locale.ROOT);
            formatted.append(normalized);
            if (i < tokens.length - 1) {
                formatted.append(' ');
            }
        }
        return formatted.toString();
    }

    private void guardLoanPolicy(int bookId, int studentId) {
        long activeLoans = circulationLedger.stream()
            .filter(tx -> tx.getStudentId() == studentId && !tx.isReturned())
            .count();

        if (activeLoans >= MAX_ACTIVE_LOANS_PER_STUDENT) {
            throw new CirculationPolicyException(
                "Student already has " + activeLoans + " active loans. Max allowed is " + MAX_ACTIVE_LOANS_PER_STUDENT + "."
            );
        }

        boolean hasSameBookActive = circulationLedger.stream()
            .anyMatch(tx -> tx.getStudentId() == studentId && tx.getBookId() == bookId && !tx.isReturned());
        if (hasSameBookActive) {
            throw new CirculationPolicyException("Student already has an active issue for this title.");
        }
    }

    private void saveData() {
        dataStore.saveBooks(catalogTitles);
        dataStore.saveStudents(studentRegistry);
        dataStore.saveIssues(circulationLedger);
    }
}


