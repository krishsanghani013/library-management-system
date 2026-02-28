package library;


public class DataStoreFormatException extends RuntimeException {
    public DataStoreFormatException(String message) {
        super(message);
    }

    public DataStoreFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}


