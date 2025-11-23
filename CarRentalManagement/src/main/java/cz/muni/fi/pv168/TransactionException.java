package cz.muni.fi.pv168;

public class TransactionException extends RuntimeException {

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException() {
    }
}
