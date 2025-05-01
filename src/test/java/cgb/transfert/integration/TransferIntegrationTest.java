package cgb.transfert.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cgb.transfert.ServerTransferApp;
import cgb.transfert.dto.TransferRequest;
import cgb.transfert.model.Account;
import cgb.transfert.repository.AccountRepository;
import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;
import cgb.transfert.config.TestSecurityConfig;
import cgb.transfert.config.TestConfig;

/**
 * Tests d'intégration pour l'API de transfert
 * Ces tests vérifient le bon fonctionnement de l'API complète avec la base de données
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestSecurityConfig.class, TestConfig.class})
public class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Générer des IBAN valides pour les tests
        String sourceIban = "FR7630001007941234567890185"; // IBAN français valide
        String destIban = "FR7630004000031234567890143"; // Autre IBAN français valide

        try {
            // Assurons-nous que les comptes de test existent
            if (accountRepository.findById(sourceIban).isEmpty()) {
                Account account1 = new Account();
                // Définir directement l'IBAN pour l'ID
                account1.setIban(sourceIban);
                account1.setSolde(300.0);
                accountRepository.save(account1);
            }

            if (accountRepository.findById(destIban).isEmpty()) {
                Account account2 = new Account();
                // Définir directement l'IBAN pour l'ID
                account2.setIban(destIban);
                account2.setSolde(500.0);
                accountRepository.save(account2);
            }
        } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
            // Dans un test, nous utilisons des IBAN valides connus, donc cette exception ne devrait pas survenir
            throw new RuntimeException("Erreur inattendue lors de la création des comptes de test", e);
        }
    }

    /**
     * Test d'intégration pour la création d'un transfert avec succès
     * Vérifie que l'API effectue correctement le transfert et met à jour les soldes
     */
    @Test
    @WithMockUser
    public void testCreateTransferIntegration() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("FR7630001007941234567890185");
        request.setDestinationAccountNumber("FR7630004000031234567890143");
        request.setAmount(100.0);
        request.setTransferDate(LocalDate.now());
        request.setDescription("Test transfer integration");

        // Soldes avant le transfert
        Account sourceAccountBefore = accountRepository.findById("FR7630001007941234567890185").orElseThrow();
        Account destAccountBefore = accountRepository.findById("FR7630004000031234567890143").orElseThrow();
        double sourceBalanceBefore = sourceAccountBefore.getSolde();
        double destBalanceBefore = destAccountBefore.getSolde();

        // Exécution du test sans le jeton CSRF
        MvcResult result = mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceIban").value("FR7630001007941234567890185"))
                .andExpect(jsonPath("$.destinationIban").value("FR7630004000031234567890143"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Test transfer integration"))
                .andReturn();

        // Vérification des soldes après le transfert
        Account sourceAccountAfter = accountRepository.findById("FR7630001007941234567890185").orElseThrow();
        Account destAccountAfter = accountRepository.findById("FR7630004000031234567890143").orElseThrow();
        
        assertEquals(sourceBalanceBefore - 100.0, sourceAccountAfter.getSolde(), 0.001);
        assertEquals(destBalanceBefore + 100.0, destAccountAfter.getSolde(), 0.001);
    }

    /**
     * Test d'intégration pour la création d'un transfert avec des fonds insuffisants
     * Vérifie que l'API retourne une erreur appropriée
     */
    @Test
    @WithMockUser
    public void testCreateTransferInsufficientFundsIntegration() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("FR7630001007941234567890185");
        request.setDestinationAccountNumber("FR7630004000031234567890143");
        request.setAmount(1000.0); // Montant supérieur au solde
        request.setTransferDate(LocalDate.now());
        request.setDescription("Test transfer integration");

        // Soldes avant le transfert
        Account sourceAccountBefore = accountRepository.findById("FR7630001007941234567890185").orElseThrow();
        Account destAccountBefore = accountRepository.findById("FR7630004000031234567890143").orElseThrow();
        double sourceBalanceBefore = sourceAccountBefore.getSolde();
        double destBalanceBefore = destAccountBefore.getSolde();

        // Exécution du test sans le jeton CSRF
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        // Vérification que les soldes n'ont pas changé
        Account sourceAccountAfter = accountRepository.findById("FR7630001007941234567890185").orElseThrow();
        Account destAccountAfter = accountRepository.findById("FR7630004000031234567890143").orElseThrow();
        
        assertEquals(sourceBalanceBefore, sourceAccountAfter.getSolde(), 0.001);
        assertEquals(destBalanceBefore, destAccountAfter.getSolde(), 0.001);
    }

    /**
     * Test d'intégration pour la création d'un transfert avec un compte source inexistant
     * Vérifie que l'API retourne une erreur appropriée
     */
    @Test
    @WithMockUser
    public void testCreateTransferSourceAccountNotFoundIntegration() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("FR7630004000031234567890144"); // Un IBAN qui n'existe pas dans le test
        request.setDestinationAccountNumber("FR7630004000031234567890143");
        request.setAmount(100.0);
        request.setTransferDate(LocalDate.now());
        request.setDescription("Test transfer integration");

        // Exécution du test sans le jeton CSRF
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Source account not found"));
    }
} 