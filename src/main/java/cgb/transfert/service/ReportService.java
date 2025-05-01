package cgb.transfert.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cgb.transfert.dto.BatchReport;
import cgb.transfert.dto.NotificationEmail;
import cgb.transfert.dto.TransferSearchCriteria;
import cgb.transfert.model.BatchTransfer;
import cgb.transfert.model.TransferBatch;
import cgb.transfert.model.BatchTransfer.TransferStatus;
import cgb.transfert.repository.BatchTransferRepository;
import cgb.transfert.repository.TransferBatchRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour générer des rapports et rechercher des transferts
 */
@Service
public class ReportService {

    @Autowired
    private TransferBatchRepository batchRepository;

    @Autowired
    private BatchTransferRepository transferRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Génère un rapport détaillé pour un lot donné
     * @param batchId L'ID du lot
     * @return Le rapport détaillé
     */
    public BatchReport generateBatchReport(Long batchId) {
        TransferBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        BatchReport report = new BatchReport();
        report.setBatchId(batch.getId());
        report.setBatchReference(batch.getBatchReference());
        report.setCreationDate(batch.getCreationDate());
        report.setStatus(batch.getStatus().toString());

        // Compter les transferts réussis et échoués
        int successCount = 0;
        int failedCount = 0;
        List<BatchReport.TransferDetail> transferDetails = new ArrayList<>();

        for (BatchTransfer transfer : batch.getTransfers()) {
            if (transfer.getStatus() == TransferStatus.SUCCESS) {
                successCount++;
            } else if (transfer.getStatus() == TransferStatus.ECHEC) {
                failedCount++;
            }

            // Ajouter les détails du transfert
            BatchReport.TransferDetail detail = new BatchReport.TransferDetail();
            detail.setTransferId(transfer.getId());
            detail.setSourceIban(transfer.getSourceIban());
            detail.setDestinationIban(transfer.getDestinationIban());
            detail.setAmount(transfer.getAmount());
            detail.setStatus(transfer.getStatus().toString());
            detail.setDescription(transfer.getDescription());
            transferDetails.add(detail);
        }

        report.setTotalTransfers(batch.getTransfers().size());
        report.setSuccessfulTransfers(successCount);
        report.setFailedTransfers(failedCount);
        report.setTransfers(transferDetails);

        return report;
    }

    /**
     * Envoie une notification par email à la fin du traitement d'un lot
     * @param batchId L'ID du lot
     * @param email L'adresse email du destinataire
     */
    public void sendBatchCompletionNotification(Long batchId, String email) {
        TransferBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Compter les transferts réussis et échoués
        int successCount = 0;
        int failedCount = 0;

        for (BatchTransfer transfer : batch.getTransfers()) {
            if (transfer.getStatus() == TransferStatus.SUCCESS) {
                successCount++;
            } else if (transfer.getStatus() == TransferStatus.ECHEC) {
                failedCount++;
            }
        }

        // Créer le contenu de l'email
        String subject = "Traitement du lot " + batch.getBatchReference() + " terminé";
        StringBuilder body = new StringBuilder();
        body.append("Le traitement du lot ").append(batch.getBatchReference()).append(" est terminé.\n\n");
        body.append("Identifiant du lot: ").append(batch.getId()).append("\n");
        body.append("Date de création: ").append(batch.getCreationDate()).append("\n");
        body.append("Nombre de transferts réussis: ").append(successCount).append("\n");
        body.append("Nombre de transferts échoués: ").append(failedCount).append("\n");

        // Envoyer l'email
        NotificationEmail notification = new NotificationEmail(email, subject, body.toString());
        emailService.sendEmail(notification);
    }

    /**
     * Recherche les transferts échoués selon différents critères
     * @param criteria Les critères de recherche
     * @return La liste des transferts échoués correspondant aux critères
     */
    public List<BatchTransfer> searchFailedTransfers(TransferSearchCriteria criteria) {
        if (!criteria.hasAnyCriteria()) {
            throw new RuntimeException("At least one search criterion must be provided");
        }

        List<BatchTransfer> failedTransfers;
        
        // Cas 1: Recherche par ID de lot
        if (criteria.getBatchId() != null) {
            failedTransfers = transferRepository.findByBatchIdAndStatus(
                criteria.getBatchId(), 
                TransferStatus.ECHEC
            );
        }
        // Cas 2: Recherche par plage de dates
        else if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            failedTransfers = transferRepository.findByStatusAndDateRange(
                TransferStatus.ECHEC,
                criteria.getStartDate(),
                criteria.getEndDate()
            );
        }
        // Cas 3: Recherche par IBAN de destination
        else if (criteria.getDestinationIban() != null && !criteria.getDestinationIban().isEmpty()) {
            failedTransfers = transferRepository.findByStatusAndDestinationIban(
                TransferStatus.ECHEC,
                criteria.getDestinationIban()
            );
        }
        // Cas 4: Par défaut, rechercher tous les transferts échoués (avec limite)
        else {
            failedTransfers = transferRepository.findByStatus(TransferStatus.ECHEC);
        }
        
        // Appliquer des filtres supplémentaires si nécessaire
        // Par exemple, si nous avons recherché par ID de lot mais avons aussi une plage de dates
        if (criteria.getStartDate() != null && (criteria.getBatchId() != null || criteria.getDestinationIban() != null)) {
            failedTransfers = failedTransfers.stream()
                .filter(transfer -> transfer.getTransferDate() != null && 
                       !transfer.getTransferDate().isBefore(criteria.getStartDate()))
                .collect(Collectors.toList());
        }
        
        if (criteria.getEndDate() != null && (criteria.getBatchId() != null || criteria.getDestinationIban() != null)) {
            failedTransfers = failedTransfers.stream()
                .filter(transfer -> transfer.getTransferDate() != null && 
                       !transfer.getTransferDate().isAfter(criteria.getEndDate()))
                .collect(Collectors.toList());
        }
        
        if (criteria.getDestinationIban() != null && !criteria.getDestinationIban().isEmpty() 
                && (criteria.getBatchId() != null || criteria.getStartDate() != null)) {
            failedTransfers = failedTransfers.stream()
                .filter(transfer -> transfer.getDestinationIban() != null && 
                       transfer.getDestinationIban().equals(criteria.getDestinationIban()))
                .collect(Collectors.toList());
        }
        
        // Limiter les résultats à 100 maximum
        if (failedTransfers.size() > 100) {
            return failedTransfers.subList(0, 100);
        }
        
        return failedTransfers;
    }
} 