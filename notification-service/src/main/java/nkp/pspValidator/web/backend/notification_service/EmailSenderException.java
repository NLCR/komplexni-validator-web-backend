package nkp.pspValidator.web.backend.notification_service;

public class EmailSenderException extends Exception {

    public EmailSenderException(String message) {
        super(message);
    }

    public EmailSenderException(Throwable cause) {
        super(cause);
    }

    public EmailSenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
