package cgb.transfert.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cgb.transfert.model.Account;
import cgb.transfert.repository.AccountRepository;
import cgb.transfert.util.IbanGenerator;
import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;

import java.util.Random;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;
    
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Initialisation des comptes si la base de données est vide
        if (accountRepository.count() == 0) {
            // Création de 20 comptes avec des IBAN valides
            initializeAccountsWithValidIbans(20);
            
            System.out.println("Base de données initialisée avec des comptes de test.");
        }
    }
    
    /**
     * Initialise un nombre spécifié de comptes avec des IBAN valides
     * @param numberOfAccounts Le nombre de comptes à créer
     */
    private void initializeAccountsWithValidIbans(int numberOfAccounts) {
        String[] countryCodes = { "FR", "DE", "IT", "ES", "BE" }; // Quelques codes pays européens
        
        for (int i = 0; i < numberOfAccounts; i++) {
            try {
                // Génère un IBAN valide avec un code pays aléatoire parmi ceux définis
                String countryCode = countryCodes[random.nextInt(countryCodes.length)];
                String iban = IbanGenerator.generateIban(countryCode);
                
                // Génère un solde aléatoire entre 100 et 10000
                double solde = 100 + random.nextDouble() * 9900;
                
                // Création et sauvegarde du compte
                Account account = new Account(iban, solde);
                accountRepository.save(account);
                
                System.out.println("Compte créé avec IBAN: " + iban + " et solde: " + solde);
            } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
                // Normalement, cela ne devrait pas arriver car nous utilisons un générateur d'IBAN valides
                System.err.println("Erreur lors de la création d'un compte: " + e.getMessage());
                // On continue avec le compte suivant
            }
        }
    }
} 