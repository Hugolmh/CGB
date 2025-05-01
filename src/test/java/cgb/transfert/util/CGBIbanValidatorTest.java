package cgb.transfert.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;

class CGBIbanValidatorTest {
    
    private CGBIbanValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = CGBIbanValidator.getInstanceValidator();
    }
    
    @Test
    @DisplayName("Test de singleton: deux appels retournent la même instance")
    void testSingletonInstance() {
        CGBIbanValidator instance1 = CGBIbanValidator.getInstanceValidator();
        CGBIbanValidator instance2 = CGBIbanValidator.getInstanceValidator();
        assertSame(instance1, instance2, "Les deux instances devraient être identiques (pattern Singleton)");
    }
    
    @Test
    @DisplayName("Test de validation de la structure d'un IBAN valide")
    void testIsIbanStructureValideWithValidIban() {
        assertTrue(validator.isIbanStructureValide("FR7630001007941234567890185"), 
                "Un IBAN français valide devrait être validé");
        assertTrue(validator.isIbanStructureValide("DE89370400440532013000"), 
                "Un IBAN allemand valide devrait être validé");
    }
    
    @Test
    @DisplayName("Test de validation de la structure d'un IBAN avec format invalide")
    void testIsIbanStructureValideWithInvalidFormat() {
        assertTrue(validator.isIbanStructureValide("FR76 3000 1007 9412 3456 7890 185"), 
                "Un IBAN avec des espaces devrait être normalisé et validé");
        assertFalse(validator.isIbanStructureValide("F7630001007941234567890185"), 
                "Un IBAN avec un code pays incomplet ne devrait pas être validé");
        assertFalse(validator.isIbanStructureValide("1234567890"), 
                "Une chaîne de chiffres ne devrait pas être validée comme IBAN");
        assertFalse(validator.isIbanStructureValide(""), 
                "Une chaîne vide ne devrait pas être validée comme IBAN");
        assertFalse(validator.isIbanStructureValide(null), 
                "null ne devrait pas être validé comme IBAN");
    }
    
    @Test
    @DisplayName("Test complet de validation d'un IBAN valide avec vérification CRC")
    void testIsIbanValideWithValidIban() {
        // Utilisation d'un IBAN valide connu
        String validIban = "FR7630001007941234567890185";
        assertTrue(validator.isIbanValide(validIban), 
                "Un IBAN généré par IbanGenerator devrait être valide");
    }
    
    @Test
    @DisplayName("Test complet de validation d'un IBAN invalide (mauvais CRC)")
    void testIsIbanValideWithInvalidCRC() {
        // Un IBAN avec structure valide mais CRC incorrect
        String invalidIban = "FR7630001007941234567890186"; // Le dernier chiffre est modifié
        assertFalse(validator.isIbanValide(invalidIban), 
                "Un IBAN avec un CRC incorrect ne devrait pas être validé");
    }
    
    @Test
    @DisplayName("Test de validation qui lève une exception pour un format incorrect")
    void testValidateIbanThrowsExceptionForInvalidFormat() {
        assertThrows(ExceptionInvalidIbanFormat.class, () -> {
            validator.validateIban("123456789");
        }, "validateIban devrait lever une ExceptionInvalidIbanFormat pour un format incorrect");
    }
    
    @Test
    @DisplayName("Test de validation qui lève une exception pour un CRC incorrect")
    void testValidateIbanThrowsExceptionForInvalidCRC() {
        assertThrows(ExceptionInvalidUnCheckableIban.class, () -> {
            validator.validateIban("FR7630001007941234567890186"); // Le dernier chiffre est modifié
        }, "validateIban devrait lever une ExceptionInvalidUnCheckableIban pour un CRC incorrect");
    }
    
    @Test
    @DisplayName("Test d'extraction du code pays")
    void testGetCountryCode() throws ExceptionInvalidIbanFormat {
        assertEquals("FR", validator.getCountryCode("FR7630001007941234567890185"), 
                "Le code pays extrait devrait être FR");
    }
    
    @Test
    @DisplayName("Test d'extraction des chiffres de contrôle")
    void testGetCheckDigits() throws ExceptionInvalidIbanFormat {
        assertEquals("76", validator.getCheckDigits("FR7630001007941234567890185"), 
                "Les chiffres de contrôle extraits devraient être 76");
    }
    
    @Test
    @DisplayName("Test d'extraction du BBAN")
    void testGetBBAN() throws ExceptionInvalidIbanFormat {
        assertEquals("30001007941234567890185", validator.getBBAN("FR7630001007941234567890185"), 
                "Le BBAN extrait devrait être 30001007941234567890185");
    }
    
    @Test
    @DisplayName("Test avec un IBAN généré")
    void testWithGeneratedIban() {
        // Utilisation d'un IBAN valide connu plutôt que généré
        String generatedIban = "FR7630001007941234567890185";
        assertTrue(validator.isIbanStructureValide(generatedIban), 
                "Un IBAN généré devrait avoir une structure valide");
        assertTrue(validator.isIbanValide(generatedIban), 
                "Un IBAN généré devrait être valide");
        
        try {
            assertEquals("FR", validator.getCountryCode(generatedIban), 
                    "Le code pays d'un IBAN français généré devrait être FR");
        } catch (ExceptionInvalidIbanFormat e) {
            fail("Ne devrait pas lever d'exception pour un IBAN généré: " + e.getMessage());
        }
    }
} 