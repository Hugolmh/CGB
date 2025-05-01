package cgb.transfert.exception;

/**
 * Exception levée lorsque l'IBAN est invalide (le CRC n'est pas conforme, 
 * ou l'IBAN est invérifiable pour d'autres raisons)
 */
public class ExceptionInvalidUnCheckableIban extends ExceptionInvalideIBAN {
    
    /**
     * Constructeur avec message
     * @param message Le message d'erreur
     */
    public ExceptionInvalidUnCheckableIban(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * @param message Le message d'erreur
     * @param cause La cause de l'exception
     */
    public ExceptionInvalidUnCheckableIban(String message, Throwable cause) {
        super(message, cause);
    }
} 