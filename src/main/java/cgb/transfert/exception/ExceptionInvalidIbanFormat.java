package cgb.transfert.exception;

/**
 * Exception levée lorsque le format de l'IBAN est incorrect
 * (nombre de caractères, code pays, etc.)
 */
public class ExceptionInvalidIbanFormat extends ExceptionInvalideIBAN {
    
    /**
     * Constructeur avec message
     * @param message Le message d'erreur
     */
    public ExceptionInvalidIbanFormat(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * @param message Le message d'erreur
     * @param cause La cause de l'exception
     */
    public ExceptionInvalidIbanFormat(String message, Throwable cause) {
        super(message, cause);
    }
} 