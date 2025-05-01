package cgb.transfert.util;

import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;

import java.util.regex.Pattern;

/**
 * Classe pour la validation des IBAN
 */
@Component
public class CGBIbanValidator {
    // Instance unique du Singleton
    private static CGBIbanValidator instance;
    
    // Validateur d'IBAN d'Apache Commons Validator
    private final IBANValidator ibanValidator;
    
    // Regex pour valider la structure d'un IBAN
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,}$");
    
    // Activer/désactiver la validation stricte des IBAN
    private static boolean strictValidation = false;
    
    /**
     * Constructeur pour l'injection Spring
     */
    public CGBIbanValidator(@Value("${cgb.iban.strict-validation:false}") boolean strictValidation) {
        this.ibanValidator = IBANValidator.getInstance();
        CGBIbanValidator.strictValidation = strictValidation;
        instance = this;
    }
    
    /**
     * Constructeur privé pour le cas où l'injection Spring n'est pas disponible
     */
    private CGBIbanValidator() {
        this.ibanValidator = IBANValidator.getInstance();
    }
    
    /**
     * Méthode pour obtenir l'instance du Singleton
     * @return L'instance du validateur d'IBAN
     */
    public static synchronized CGBIbanValidator getInstanceValidator() {
        if (instance == null) {
            instance = new CGBIbanValidator();
        }
        return instance;
    }
    
    /**
     * Vérifie que la structure de l'IBAN respecte les règles syntaxiques de base
     * @param iban L'IBAN à vérifier
     * @return true si la structure de l'IBAN est valide, false sinon
     */
    public boolean isIbanStructureValide(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }
        
        // Supprimer les espaces et convertir en majuscules
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        
        // Vérifier avec le regex - IBAN doit être sans espaces
        return IBAN_PATTERN.matcher(normalizedIban).matches();
    }
    
    /**
     * Vérifie que l'IBAN est valide, en incluant la vérification du CRC
     * @param iban L'IBAN à vérifier
     * @return true si l'IBAN est valide, false sinon
     */
    public boolean isIbanValide(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }
        
        // Supprimer les espaces et convertir en majuscules
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        
        // Si la validation stricte est désactivée, on considère tout IBAN avec une structure valide comme valide
        if (!strictValidation) {
            return isIbanStructureValide(normalizedIban);
        }
        
        // Sinon, on utilise le validateur d'Apache Commons
        return ibanValidator.isValid(normalizedIban);
    }
    
    /**
     * Vérifie l'IBAN et lève une exception si l'IBAN est invalide
     * @param iban L'IBAN à vérifier
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     * @throws ExceptionInvalidUnCheckableIban si l'IBAN est invalide (CRC non conforme, etc.)
     */
    public void validateIban(String iban) throws ExceptionInvalidIbanFormat, ExceptionInvalidUnCheckableIban {
        if (iban == null || iban.isEmpty()) {
            throw new ExceptionInvalidIbanFormat("L'IBAN est null ou vide");
        }
        
        // Supprimer les espaces et convertir en majuscules
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        
        // Vérifier le format
        if (!isIbanStructureValide(normalizedIban)) {
            throw new ExceptionInvalidIbanFormat("Format de l'IBAN incorrect: " + iban);
        }
        
        // Si la validation stricte est activée, on vérifie aussi la validité avec le CRC
        if (strictValidation && !isIbanValide(normalizedIban)) {
            throw new ExceptionInvalidUnCheckableIban("IBAN invalide (vérification CRC échouée): " + iban);
        }
    }
    
    /**
     * Extrait le code pays de l'IBAN
     * @param iban L'IBAN
     * @return Le code pays (2 lettres)
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     */
    public String getCountryCode(String iban) throws ExceptionInvalidIbanFormat {
        if (!isIbanStructureValide(iban)) {
            throw new ExceptionInvalidIbanFormat("Format de l'IBAN incorrect: " + iban);
        }
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        return normalizedIban.substring(0, 2);
    }
    
    /**
     * Extrait les chiffres de contrôle de l'IBAN
     * @param iban L'IBAN
     * @return Les chiffres de contrôle (2 chiffres)
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     */
    public String getCheckDigits(String iban) throws ExceptionInvalidIbanFormat {
        if (!isIbanStructureValide(iban)) {
            throw new ExceptionInvalidIbanFormat("Format de l'IBAN incorrect: " + iban);
        }
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        return normalizedIban.substring(2, 4);
    }
    
    /**
     * Extrait le Basic Bank Account Number (BBAN) de l'IBAN
     * @param iban L'IBAN
     * @return Le BBAN
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     */
    public String getBBAN(String iban) throws ExceptionInvalidIbanFormat {
        if (!isIbanStructureValide(iban)) {
            throw new ExceptionInvalidIbanFormat("Format de l'IBAN incorrect: " + iban);
        }
        String normalizedIban = iban.replaceAll("\\s", "").toUpperCase();
        return normalizedIban.substring(4);
    }
} 