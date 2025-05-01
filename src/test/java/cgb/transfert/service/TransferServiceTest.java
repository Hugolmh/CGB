package cgb.transfert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgb.transfert.model.Account;
import cgb.transfert.model.Transfer;
import cgb.transfert.repository.AccountRepository;
import cgb.transfert.repository.TransferRepository;
import cgb.transfert.util.CGBIbanValidator;

/**
 * Tests unitaires pour le service de transfert
 * Ces tests vérifient la logique métier du service de transfert
 */
@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;
    
    @Mock
    private CGBIbanValidator ibanValidator;

    @InjectMocks
    private TransferService transferService;

    private Account sourceAccount;
    private Account destinationAccount;
    private Transfer transfer;

    @BeforeEach
    public void setup() {
        // Initialisation des comptes de test
        sourceAccount = new Account();
        sourceAccount.setAccountNumber("FR7630001007941234567890185");
        sourceAccount.setSolde(300.0);

        destinationAccount = new Account();
        destinationAccount.setAccountNumber("FR7630006000011234567890189");
        destinationAccount.setSolde(500.0);

        // Initialisation d'un transfert de test
        transfer = new Transfer();
        transfer.setId(1L);
        transfer.setSourceIban("FR7630001007941234567890185");
        transfer.setDestinationIban("FR7630006000011234567890189");
        transfer.setAmount(100.0);
        transfer.setTransferDate(LocalDate.now());
        transfer.setDescription("Test transfer");
    }

    /**
     * Test de création d'un transfert avec succès
     * Vérifie que le service effectue correctement le transfert et met à jour les soldes
     */
    @Test
    public void testCreateTransferSuccess() {
        // Configuration des mocks
        when(accountRepository.findById("FR7630001007941234567890185")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("FR7630006000011234567890189")).thenReturn(Optional.of(destinationAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        // Exécution du test
        Transfer result = transferService.createTransfer(
                "FR7630001007941234567890185", "FR7630006000011234567890189", 100.0, LocalDate.now(), "Test transfer");

        // Vérifications
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FR7630001007941234567890185", result.getSourceIban());
        assertEquals("FR7630006000011234567890189", result.getDestinationIban());
        assertEquals(100.0, result.getAmount());
        assertEquals("Test transfer", result.getDescription());

        // Vérification des mises à jour des soldes
        assertEquals(200.0, sourceAccount.getSolde()); // 300 - 100
        assertEquals(600.0, destinationAccount.getSolde()); // 500 + 100

        // Vérification des appels aux repositories
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(destinationAccount);
        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    /**
     * Test de création d'un transfert avec un compte source inexistant
     * Vérifie que le service lève une exception appropriée
     */
    @Test
    public void testCreateTransferSourceAccountNotFound() {
        // Configuration des mocks
        String invalidIban = "FR7630001007941234567890186";
        when(accountRepository.findById(invalidIban)).thenReturn(Optional.empty());

        // Exécution du test et vérification
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.createTransfer(invalidIban, "FR7630006000011234567890189", 100.0, LocalDate.now(), "Test transfer");
        });

        assertEquals("Source account not found", exception.getMessage());
    }

    /**
     * Test de création d'un transfert avec un compte destination inexistant
     * Vérifie que le service lève une exception appropriée
     */
    @Test
    public void testCreateTransferDestinationAccountNotFound() {
        // Configuration des mocks
        String invalidIban = "FR7630006000011234567890180";
        when(accountRepository.findById("FR7630001007941234567890185")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(invalidIban)).thenReturn(Optional.empty());

        // Exécution du test et vérification
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.createTransfer("FR7630001007941234567890185", invalidIban, 100.0, LocalDate.now(), "Test transfer");
        });

        assertEquals("Destination account not found", exception.getMessage());
    }

    /**
     * Test de création d'un transfert avec des fonds insuffisants
     * Vérifie que le service lève une exception appropriée
     */
    @Test
    public void testCreateTransferInsufficientFunds() {
        // Configuration des mocks
        when(accountRepository.findById("FR7630001007941234567890185")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("FR7630006000011234567890189")).thenReturn(Optional.of(destinationAccount));

        // Exécution du test et vérification
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.createTransfer("FR7630001007941234567890185", "FR7630006000011234567890189", 500.0, LocalDate.now(), "Test transfer");
        });

        assertEquals("Insufficient funds", exception.getMessage());
    }
} 