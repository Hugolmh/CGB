package cgb.transfert.exception;

/**
 * Classe abstraite pour les exceptions liées à la validation des IBAN
 */
public abstract class ExceptionInvalideIBAN extends Exception {
    
    /**
     * Constructeur avec message
     * @param message Le message d'erreur
     */
    public ExceptionInvalideIBAN(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * @param message Le message d'erreur
     * @param cause La cause de l'exception
     */
    public ExceptionInvalideIBAN(String message, Throwable cause) {
        super(message, cause);
    }
} 