package cgb.transfert.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

import cgb.transfert.model.Account;
import cgb.transfert.model.Transfer;
import cgb.transfert.repository.AccountRepository;
import cgb.transfert.repository.TransferRepository;
import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;
import cgb.transfert.util.CGBIbanValidator;

@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private CGBIbanValidator ibanValidator;
    
    /*
     * Méthode pour créer un transfert entre deux comptes, en vérifiant la validité des IBAN
     */
    @Transactional
    public Transfer createTransfer(String sourceIban, String destinationIban,
                                   Double amount, LocalDate transferDate, String description) {
        // Validation des IBAN
        try {
            ibanValidator.validateIban(sourceIban);
            ibanValidator.validateIban(destinationIban);
        } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
            throw new RuntimeException("Invalid IBAN: " + e.getMessage());
        }
        
        // Recherche des comptes
        Account sourceAccount = accountRepository.findById(sourceIban)
                				.orElseThrow(() -> new RuntimeException("Source account not found"));
        Account destinationAccount = accountRepository.findById(destinationIban)
                				.orElseThrow(() -> new RuntimeException("Destination account not found"));

        /*Pas de découvert autorisé*/
        if (sourceAccount.getSolde().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Mise à jour des soldes
        sourceAccount.setSolde(sourceAccount.getSolde()-(amount)); 
        destinationAccount.setSolde(destinationAccount.getSolde()+(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // Création et enregistrement du transfert
        Transfer transfer = new Transfer();
        transfer.setSourceIban(sourceIban);
        transfer.setDestinationIban(destinationIban);
        transfer.setAmount(amount);
        transfer.setTransferDate(transferDate);
        transfer.setDescription(description);

        return transferRepository.save(transfer);
    }
    
    // Méthode de compatibilité pour l'ancien code
    @Deprecated
    @Transactional
    public Transfer createTransferWithAccountNumbers(String sourceAccountNumber, String destinationAccountNumber,
                                   Double amount, LocalDate transferDate, String description) {
        // Réutilisation de la méthode principale (les numéros de compte sont considérés comme des IBAN)
        return createTransfer(sourceAccountNumber, destinationAccountNumber, amount, transferDate, description);
    }
} 