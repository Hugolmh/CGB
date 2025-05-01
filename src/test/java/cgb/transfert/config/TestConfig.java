package cgb.transfert.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import cgb.transfert.util.CGBIbanValidator;

/**
 * Configuration spécifique pour les tests
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Fournit un validateur d'IBAN qui accepte toujours les IBAN de test
     */
    @Bean
    @Primary
    public CGBIbanValidator ibanValidator() {
        // Récupérer l'instance singleton
        return CGBIbanValidator.getInstanceValidator();
    }
} 