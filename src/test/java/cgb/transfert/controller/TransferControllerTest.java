package cgb.transfert.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cgb.transfert.dto.TransferRequest;
import cgb.transfert.model.Transfer;
import cgb.transfert.service.TransferService;
import cgb.transfert.config.TestSecurityConfig;

/**
 * Tests unitaires pour le contrôleur de transfert
 * Ces tests vérifient le bon fonctionnement des endpoints de l'API
 */
@WebMvcTest(TransferController.class) // Indique que nous testons le contrôleur TransferController
@Import(TestSecurityConfig.class) // Importe la configuration de sécurité pour les tests
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permet d'effectuer des requêtes HTTP sur le contrôleur

    @MockBean
    private TransferService transferService; // Simule le service de transfert pour les tests

    private ObjectMapper objectMapper; // Utilisé pour convertir les objets en JSON

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper(); // Initialise l'ObjectMapper
        objectMapper.registerModule(new JavaTimeModule()); // Enregistre le module pour gérer les dates
    }

    /**
     * Test de création d'un transfert avec succès
     * Vérifie que l'API retourne un statut 200 OK et les détails du transfert
     */
    @Test
    @WithMockUser // Simule un utilisateur authentifié
    public void testCreateTransferSuccess() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest(); // Crée une nouvelle requête de transfert
        request.setSourceAccountNumber("123456789"); // Numéro de compte source
        request.setDestinationAccountNumber("987654321"); // Numéro de compte destination
        request.setAmount(100.0); // Montant du transfert
        request.setTransferDate(LocalDate.now()); // Date du transfert
        request.setDescription("Test transfer"); // Description du transfert

        // Création d'un transfert simulé pour le retour du service
        Transfer mockTransfer = new Transfer(); // Crée un objet Transfer simulé
        mockTransfer.setId(1L); // Définit l'ID du transfert
        mockTransfer.setSourceAccountNumber("123456789"); // Définit le numéro de compte source
        mockTransfer.setDestinationAccountNumber("987654321"); // Définit le numéro de compte destination
        mockTransfer.setAmount(100.0); // Définit le montant du transfert
        mockTransfer.setTransferDate(LocalDate.now()); // Définit la date du transfert
        mockTransfer.setDescription("Test transfer"); // Définit la description du transfert

        // Configuration du mock du service pour retourner le transfert simulé
        when(transferService.createTransfer(
                anyString(), anyString(), anyDouble(), any(LocalDate.class), anyString()))
                .thenReturn(mockTransfer); // Simule le comportement du service

        // Exécution du test sans le jeton CSRF (désactivé dans TestSecurityConfig)
        mockMvc.perform(post("/api/transfers") // Effectue une requête POST sur l'endpoint /api/transfers
                .contentType(MediaType.APPLICATION_JSON) // Définit le type de contenu à JSON
                .content(objectMapper.writeValueAsString(request))) // Convertit la requête en JSON
                .andExpect(status().isOk()) // Vérifie que le statut de la réponse est 200 OK
                .andExpect(jsonPath("$.id").value(1)) // Vérifie que l'ID du transfert est correct
                .andExpect(jsonPath("$.sourceAccountNumber").value("123456789")) // Vérifie le numéro de compte source
                .andExpect(jsonPath("$.destinationAccountNumber").value("987654321")) // Vérifie le numéro de compte destination
                .andExpect(jsonPath("$.amount").value(100.0)) // Vérifie le montant du transfert
                .andExpect(jsonPath("$.description").value("Test transfer")); // Vérifie la description du transfert
    }

    /**
     * Test de création d'un transfert avec un compte source inexistant
     * Vérifie que l'API retourne un statut 400 Bad Request et un message d'erreur approprié
     */
    @Test
    @WithMockUser // Simule un utilisateur authentifié
    public void testCreateTransferSourceAccountNotFound() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest(); // Crée une nouvelle requête de transfert
        request.setSourceAccountNumber("invalid"); // Numéro de compte source invalide
        request.setDestinationAccountNumber("987654321"); // Numéro de compte destination
        request.setAmount(100.0); // Montant du transfert
        request.setTransferDate(LocalDate.now()); // Date du transfert
        request.setDescription("Test transfer"); // Description du transfert

        // Configuration du mock du service pour simuler une erreur
        when(transferService.createTransfer(
                anyString(), anyString(), anyDouble(), any(LocalDate.class), anyString()))
                .thenThrow(new RuntimeException("Source account not found")); // Simule une exception

        // Exécution du test sans le jeton CSRF (désactivé dans TestSecurityConfig)
        mockMvc.perform(post("/api/transfers") // Effectue une requête POST sur l'endpoint /api/transfers
                .contentType(MediaType.APPLICATION_JSON) // Définit le type de contenu à JSON
                .content(objectMapper.writeValueAsString(request))) // Convertit la requête en JSON
                .andExpect(status().isBadRequest()) // Vérifie que le statut de la réponse est 400 Bad Request
                .andExpect(jsonPath("$.status").value("FAILURE")) // Vérifie que le statut de la réponse est "FAILURE"
                .andExpect(jsonPath("$.message").value("Source account not found")); // Vérifie le message d'erreur
    }

    /**
     * Test de création d'un transfert avec des fonds insuffisants
     * Vérifie que l'API retourne un statut 400 Bad Request et un message d'erreur approprié
     */
    @Test
    @WithMockUser // Simule un utilisateur authentifié
    public void testCreateTransferInsufficientFunds() throws Exception {
        // Préparation des données de test
        TransferRequest request = new TransferRequest(); // Crée une nouvelle requête de transfert
        request.setSourceAccountNumber("123456789"); // Numéro de compte source
        request.setDestinationAccountNumber("987654321"); // Numéro de compte destination
        request.setAmount(1000.0); // Montant supérieur au solde
        request.setTransferDate(LocalDate.now()); // Date du transfert
        request.setDescription("Test transfer"); // Description du transfert

        // Configuration du mock du service pour simuler une erreur
        when(transferService.createTransfer(
                anyString(), anyString(), anyDouble(), any(LocalDate.class), anyString()))
                .thenThrow(new RuntimeException("Insufficient funds")); // Simule une exception

        // Exécution du test sans le jeton CSRF (désactivé dans TestSecurityConfig)
        mockMvc.perform(post("/api/transfers") // Effectue une requête POST sur l'endpoint /api/transfers
                .contentType(MediaType.APPLICATION_JSON) // Définit le type de contenu à JSON
                .content(objectMapper.writeValueAsString(request))) // Convertit la requête en JSON
                .andExpect(status().isBadRequest()) // Vérifie que le statut de la réponse est 400 Bad Request
                .andExpect(jsonPath("$.status").value("FAILURE")) // Vérifie que le statut de la réponse est "FAILURE"
                .andExpect(jsonPath("$.message").value("Insufficient funds")); // Vérifie le message d'erreur
    }
}