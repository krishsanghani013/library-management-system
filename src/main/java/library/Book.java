package library;


public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final String genreCode;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, String genreCode, int totalCopies, int availableCopies) {
        if (id <= 0) {
            throw new DataStoreFormatException("Book id must be positive. Found: " + id);
        }
        if (title == null || title.isBlank()) {
            throw new DataStoreFormatException("Book title cannot be blank for id " + id);
        }
        if (author == null || author.isBlank()) {
            throw new DataStoreFormatException("Book author cannot be blank for id " + id);
        }
        if (genreCode == null || genreCode.isBlank()) {
            throw new DataStoreFormatException("Book genre section cannot be blank for id " + id);
        }
        if (totalCopies < 0 || availableCopies < 0 || availableCopies > totalCopies) {
            throw new DataStoreFormatException(
                "Invalid stock figures for book id " + id + " (total=" + totalCopies + ", available=" + availableCopies + ")"
            );
        }

        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.genreCode = genreCode.trim().toUpperCase();
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenreCode() {
        return genreCode;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void increaseCopies(int count) {
        if (count <= 0) {
            throw new CirculationPolicyException("Stock increment must be greater than zero.");
        }
        totalCopies += count;
        availableCopies += count;
    }

    public boolean issueOneCopy() {
        if (availableCopies <= 0) {
            return false;
        }
        availableCopies--;
        return true;
    }

    public boolean returnOneCopy() {
        if (availableCopies >= totalCopies) {
            return false;
        }
        availableCopies++;
        return true;
    }
}


