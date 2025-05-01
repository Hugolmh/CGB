package cgb.transfert.dto;

import java.util.List;

/**
 * DTO pour la requête de création d'un lot de virements
 */
public class BatchTransferRequest {
    private String sourceIban;
    private String batchReference;
    private List<BatchTransferItem> transfers;

    // Getters and Setters
    public String getSourceIban() {
        return sourceIban;
    }

    public void setSourceIban(String sourceIban) {
        this.sourceIban = sourceIban;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }

    public List<BatchTransferItem> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<BatchTransferItem> transfers) {
        this.transfers = transfers;
    }

    /**
     * Classe interne représentant un élément de transfert dans le lot
     */
    public static class BatchTransferItem {
        private String destinationIban;
        private Double amount;
        private String description;

        // Getters and Setters
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
} 