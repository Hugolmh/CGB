package cgb.transfert.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour le rapport détaillé d'un lot de virements
 */
public class BatchReport {
    private Long batchId;
    private String batchReference;
    private LocalDateTime creationDate;
    private String status;
    private int totalTransfers;
    private int successfulTransfers;
    private int failedTransfers;
    private List<TransferDetail> transfers;

    // Constructeurs, getters et setters
    public BatchReport() {
    }

    // Classe interne pour les détails des transferts
    public static class TransferDetail {
        private Long transferId;
        private String sourceIban;
        private String destinationIban;
        private Double amount;
        private String status;
        private String description;

        // Constructeurs, getters et setters
        public TransferDetail() {
        }

        public Long getTransferId() {
            return transferId;
        }

        public void setTransferId(Long transferId) {
            this.transferId = transferId;
        }

        public String getSourceIban() {
            return sourceIban;
        }

        public void setSourceIban(String sourceIban) {
            this.sourceIban = sourceIban;
        }

        public String getDestinationIban() {
            return destinationIban;
        }

        public void setDestinationIban(String destinationIban) {
            this.destinationIban = destinationIban;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Getters et setters pour la classe principale
    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(int totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public int getSuccessfulTransfers() {
        return successfulTransfers;
    }

    public void setSuccessfulTransfers(int successfulTransfers) {
        this.successfulTransfers = successfulTransfers;
    }

    public int getFailedTransfers() {
        return failedTransfers;
    }

    public void setFailedTransfers(int failedTransfers) {
        this.failedTransfers = failedTransfers;
    }

    public List<TransferDetail> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<TransferDetail> transfers) {
        this.transfers = transfers;
    }
} 