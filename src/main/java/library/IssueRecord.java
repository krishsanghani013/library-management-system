package library;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;


public class IssueRecord {
    private final int issueId;
    private final int bookId;
    private final int studentId;
    private final LocalDate issueDate;
    private LocalDate returnDate;
    private boolean returned;

    public IssueRecord(int issueId, int bookId, int studentId, String issueDate, String returnDate, boolean returned) {
        this(issueId, bookId, studentId, parseDate(issueDate, "issueDate"), parseOptionalDate(returnDate), returned);
    }

    public IssueRecord(int issueId, int bookId, int studentId, LocalDate issueDate) {
        this(issueId, bookId, studentId, issueDate, null, false);
    }

    
    public IssueRecord(int issueId, int bookId, int studentId, LocalDate issueDate, LocalDate returnDate, boolean returned) {
        validateIds(issueId, bookId, studentId);
        if (issueDate == null) {
            throw new CirculationPolicyException("Issue date cannot be null.");
        }

        if (returned && returnDate == null) {
            throw new CirculationPolicyException("Return date is required when item is flagged returned.");
        }
        if (returnDate != null && returnDate.isBefore(issueDate)) {
            throw new CirculationPolicyException("Return date cannot be earlier than issue date.");
        }

        this.issueId = issueId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.returnDate = returned ? returnDate : null;
        this.returned = returned;
    }

    public int getIssueId() {
        return issueId;
    }

    public int getBookId() {
        return bookId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getIssueDate() {
        return issueDate.toString();
    }

    
    public LocalDate getIssueDateValue() {
        return issueDate;
    }

    public String getReturnDate() {
        return returnDate == null ? "" : returnDate.toString();
    }

    public LocalDate getReturnDateValue() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned(String returnDate) {
        markReturned(parseDate(returnDate, "returnDate"));
    }

    
    public void markReturned(LocalDate returnDate) {
        if (returned) {
            throw new CirculationPolicyException("This transaction is already closed.");
        }
        if (returnDate == null) {
            throw new CirculationPolicyException("Return date is required.");
        }
        if (returnDate.isBefore(issueDate)) {
            throw new CirculationPolicyException("Return date cannot be earlier than issue date.");
        }

        this.returnDate = returnDate;
        this.returned = true;
    }

    public boolean isOverdue(int maxBorrowDays, LocalDate asOfDate) {
        if (maxBorrowDays <= 0) {
            throw new CirculationPolicyException("Borrow window must be greater than zero days.");
        }
        LocalDate referenceDate = asOfDate != null ? asOfDate : LocalDate.now();
        LocalDate endDate = returned ? returnDate : referenceDate;
        long borrowedDays = ChronoUnit.DAYS.between(issueDate, endDate);
        return borrowedDays > maxBorrowDays;
    }

    private static void validateIds(int issueId, int bookId, int studentId) {
        if (issueId <= 0 || bookId <= 0 || studentId <= 0) {
            throw new DataStoreFormatException("Issue, book and student IDs must be positive integers.");
        }
    }

    private static LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DataStoreFormatException(fieldName + " cannot be empty.");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new DataStoreFormatException(fieldName + " must use yyyy-MM-dd format.", ex);
        }
    }

    private static LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseDate(value, "returnDate");
    }
}


