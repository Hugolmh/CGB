package cgb.transfert.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cgb.transfert.dto.BatchResponse;
import cgb.transfert.dto.BatchTransferRequest;
import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;
import cgb.transfert.model.Account;
import cgb.transfert.model.BatchTransfer;
import cgb.transfert.model.TransferBatch;
import cgb.transfert.model.BatchTransfer.TransferStatus;
import cgb.transfert.model.TransferBatch.BatchStatus;
import cgb.transfert.repository.AccountRepository;
import cgb.transfert.repository.BatchTransferRepository;
import cgb.transfert.repository.TransferBatchRepository;
import cgb.transfert.util.CGBIbanValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour le traitement des lots de virements
 */
@Service
public class BatchTransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferBatchRepository batchRepository;

    @Autowired
    private BatchTransferRepository batchTransferRepository;
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private CGBIbanValidator ibanValidator;
    
    @Value("${notification.email:gogolamarche@gmail.com}")
    private String notificationEmail;

    /**
     * Crée un nouveau lot de virements
     * @param request La requête contenant les informations du lot
     * @return Une réponse contenant l'ID du lot et son statut
     */
    public BatchResponse createBatch(BatchTransferRequest request) {
        // Vérifier si le compte source existe
        String sourceIban = request.getSourceIban();
        
        // Valider l'IBAN source
        try {
            ibanValidator.validateIban(sourceIban);
        } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
            throw new RuntimeException("Invalid source IBAN: " + e.getMessage());
        }
        
        Optional<Account> sourceAccount = accountRepository.findById(sourceIban);
        
        if (sourceAccount.isEmpty()) {
            throw new RuntimeException("Source account not found");
        }

        // Utiliser la référence fournie par l'utilisateur ou générer une basée sur la date actuelle
        String batchReference;
        if (request.getBatchReference() != null && !request.getBatchReference().isEmpty()) {
            // Format: Date actuelle + la référence fournie par l'utilisateur
            LocalDate today = LocalDate.now();
            batchReference = today.format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + request.getBatchReference();
        } else {
            // Si aucune référence n'est fournie, utiliser la date actuelle avec un timestamp
            batchReference = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + System.currentTimeMillis();
        }
        
        // Vérifier si un lot avec cette référence existe déjà
        if (batchRepository.existsByBatchReference(batchReference)) {
            throw new RuntimeException("Batch with reference " + batchReference + " already exists");
        }

        // Créer le lot
        TransferBatch batch = new TransferBatch();
        batch.setCreationDate(LocalDateTime.now());
        batch.setStatus(BatchStatus.EN_COURS);
        batch.setBatchReference(batchReference);

        // Créer les transferts individuels
        for (BatchTransferRequest.BatchTransferItem item : request.getTransfers()) {
            // Valider l'IBAN de destination
            try {
                ibanValidator.validateIban(item.getDestinationIban());
            } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
                throw new RuntimeException("Invalid destination IBAN: " + e.getMessage());
            }
            
            BatchTransfer transfer = new BatchTransfer();
            transfer.setSourceIban(sourceIban);
            transfer.setDestinationIban(item.getDestinationIban());
            transfer.setAmount(item.getAmount());
            transfer.setTransferDate(LocalDate.now());
            transfer.setDescription(item.getDescription());
            transfer.setStatus(TransferStatus.ATTENTE);
            
            batch.addTransfer(transfer);
        }

        // Sauvegarder le lot
        TransferBatch savedBatch = batchRepository.save(batch);

        // Lancer le traitement du lot en asynchrone
        processBatchAsync(savedBatch.getId());

        // Retourner la réponse immédiate
        return new BatchResponse(
            savedBatch.getId(),
            savedBatch.getCreationDate(),
            "Traitement Lancé",
            "EnCours"
        );
    }

    /**
     * Traitement asynchrone du lot de virements
     * @param batchId L'ID du lot à traiter
     */
    @Async
    public void processBatchAsync(Long batchId) {
        System.out.println("Début du traitement asynchrone du lot " + batchId);
        try {
            // Récupérer le lot
            TransferBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
            System.out.println("Lot " + batchId + " récupéré, traitement des transferts...");

            // Traiter chaque transfert individuellement
            for (BatchTransfer transfer : batch.getTransfers()) {
                System.out.println("Traitement du transfert " + transfer.getId());
                processTransfer(transfer);
            }

            // Mettre à jour le statut du lot
            batch.setStatus(BatchStatus.TERMINE);
            batchRepository.save(batch);
            System.out.println("Lot " + batchId + " terminé, envoi de la notification...");
            
            // Envoyer une notification par email
            reportService.sendBatchCompletionNotification(batchId, notificationEmail);
            System.out.println("Notification envoyée pour le lot " + batchId);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du lot " + batchId + ": " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, mettre à jour le statut du lot
            TransferBatch batch = batchRepository.findById(batchId).orElse(null);
            if (batch != null) {
                batch.setStatus(BatchStatus.ECHEC);
                batchRepository.save(batch);
                System.out.println("Statut du lot " + batchId + " mis à jour à ECHEC");
                
                // Envoyer une notification d'échec
                try {
                    reportService.sendBatchCompletionNotification(batchId, notificationEmail);
                    System.out.println("Notification d'échec envoyée pour le lot " + batchId);
                } catch (Exception emailError) {
                    System.err.println("Erreur lors de l'envoi de la notification d'échec : " + emailError.getMessage());
                    emailError.printStackTrace();
                }
            }
        }
    }

    /**
     * Traite un transfert individuel
     * @param transfer Le transfert à traiter
     */
    @Transactional
    public void processTransfer(BatchTransfer transfer) {
        try {
            // Récupérer les comptes
            Account sourceAccount = accountRepository.findById(transfer.getSourceIban())
                .orElseThrow(() -> new RuntimeException("Source account not found"));
            
            Account destinationAccount = accountRepository.findById(transfer.getDestinationIban())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

            // Vérifier si le compte source a suffisamment de fonds
            if (sourceAccount.getSolde() < transfer.getAmount()) {
                transfer.setStatus(TransferStatus.ECHEC);
                batchTransferRepository.save(transfer);
                return;
            }

            // Effectuer le transfert
            sourceAccount.setSolde(sourceAccount.getSolde() - transfer.getAmount());
            destinationAccount.setSolde(destinationAccount.getSolde() + transfer.getAmount());

            // Sauvegarder les comptes
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            // Mettre à jour le statut du transfert
            transfer.setStatus(TransferStatus.SUCCESS);
            batchTransferRepository.save(transfer);
        } catch (Exception e) {
            // En cas d'erreur, mettre à jour le statut du transfert
            transfer.setStatus(TransferStatus.ECHEC);
            batchTransferRepository.save(transfer);
        }
    }

    /**
     * Rejoue les transferts en échec d'un lot
     * @param batchId L'ID du lot
     * @return Le nombre de transferts rejoués
     */
    @Transactional
    public int replayFailedTransfers(Long batchId) {
        List<BatchTransfer> failedTransfers = batchTransferRepository.findByBatchIdAndStatus(batchId, TransferStatus.ECHEC);
        
        for (BatchTransfer transfer : failedTransfers) {
            transfer.setStatus(TransferStatus.ATTENTE);
            batchTransferRepository.save(transfer);
            processTransfer(transfer);
        }
        
        return failedTransfers.size();
    }

    /**
     * Annule un transfert
     * @param transferId L'ID du transfert à annuler
     * @return Le transfert annulé
     */
    @Transactional
    public BatchTransfer cancelTransfer(Long transferId) {
        BatchTransfer transfer = batchTransferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("Transfer not found"));
        
        if (transfer.getStatus() == TransferStatus.SUCCESS) {
            // Si le transfert a déjà été effectué, il faut annuler l'opération
            try {
                Account sourceAccount = accountRepository.findById(transfer.getSourceIban())
                    .orElseThrow(() -> new RuntimeException("Source account not found"));
                
                Account destinationAccount = accountRepository.findById(transfer.getDestinationIban())
                    .orElseThrow(() -> new RuntimeException("Destination account not found"));

                // Annuler le transfert
                sourceAccount.setSolde(sourceAccount.getSolde() + transfer.getAmount());
                destinationAccount.setSolde(destinationAccount.getSolde() - transfer.getAmount());

                // Sauvegarder les comptes
                accountRepository.save(sourceAccount);
                accountRepository.save(destinationAccount);
            } catch (Exception e) {
                throw new RuntimeException("Failed to cancel transfer: " + e.getMessage());
            }
        }
        
        // Mettre à jour le statut du transfert
        transfer.setStatus(TransferStatus.CANCELED);
        return batchTransferRepository.save(transfer);
    }

    /**
     * Récupère un lot par son ID
     * @param batchId L'ID du lot
     * @return Le lot
     */
    public TransferBatch getBatchById(Long batchId) {
        return batchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found"));
    }

    /**
     * Récupère un transfert par son ID
     * @param transferId L'ID du transfert
     * @return Le transfert
     */
    public BatchTransfer getTransferById(Long transferId) {
        return batchTransferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("Transfer not found"));
    }
} 