package cgb.transfert.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * Classe utilitaire pour générer des IBAN valides
 */
public class IbanGenerator {
    
    private static final Random random = new Random();
    
    /**
     * Génère un IBAN valide pour un pays spécifique
     * @param countryCode Le code pays (FR, DE, ES, etc.)
     * @return Un IBAN valide
     */
    public static String generateIban(String countryCode) {
        // Génération d'un BBAN aléatoire 
        // (en général, cela dépend du pays mais on garde une structure simple ici)
        StringBuilder bban = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            bban.append(random.nextInt(10));
        }
        
        // Calcul des chiffres de contrôle
        String checkDigits = calculateCheckDigits(countryCode, bban.toString());
        
        // Construction de l'IBAN complet
        return countryCode + checkDigits + bban.toString();
    }
    
    /**
     * Génère un IBAN français valide
     * @return Un IBAN français valide
     */
    public static String generateFrenchIban() {
        return generateIban("FR");
    }
    
    /**
     * Calcule les chiffres de contrôle d'un IBAN selon l'algorithme MOD-97-10
     * @param countryCode Le code pays
     * @param bban Le BBAN
     * @return Les chiffres de contrôle (2 chiffres)
     */
    private static String calculateCheckDigits(String countryCode, String bban) {
        // Conversion du countryCode en chiffres (A=10, B=11, etc.)
        StringBuilder numericCode = new StringBuilder();
        for (char c : countryCode.toCharArray()) {
            // Conversion des lettres en chiffres (A=10, B=11, ..., Z=35)
            int value = Character.getNumericValue(c);
            if (value < 0 || value > 35) {
                throw new IllegalArgumentException("Caractère invalide dans le code pays: " + c);
            }
            numericCode.append(value);
        }
        
        // Construction du nombre pour le calcul du modulo
        // Format: BBAN + CountryCode convertis + "00"
        String modString = bban + numericCode.toString() + "00";
        
        // Conversion en BigInteger pour le calcul du modulo
        BigInteger mod = new BigInteger(modString);
        BigInteger remainder = mod.mod(BigInteger.valueOf(97));
        
        // Les chiffres de contrôle sont 98 moins le reste
        int checksum = 98 - remainder.intValue();
        
        // Formatage pour avoir deux chiffres (avec zéro en préfixe si nécessaire)
        return String.format("%02d", checksum);
    }

    /**
     * Méthode de test pour générer un IBAN français valide statique
     * Utilisée pour les tests
     * @return Un IBAN français valide pour les tests
     */
    public static String generateValidTestIban() {
        return "FR7630001007941234567890185";
    }
} 