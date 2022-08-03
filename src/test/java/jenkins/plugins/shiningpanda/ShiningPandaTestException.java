package jenkins.plugins.shiningpanda;

public class ShiningPandaTestException extends RuntimeException {
    public ShiningPandaTestException() {
    }

    public ShiningPandaTestException(String message) {
        super(message);
    }

    public ShiningPandaTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShiningPandaTestException(Throwable cause) {
        super(cause);
    }

    public ShiningPandaTestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
