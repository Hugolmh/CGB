package cgb.transfert.dto;

import java.time.LocalDate;

/**
 * DTO pour les critères de recherche des transferts échoués
 */
public class TransferSearchCriteria {
    private Long batchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destinationIban;
    
    // Constructeurs
    public TransferSearchCriteria() {
    }
    
    // Getters et setters
    public Long getBatchId() {
        return batchId;
    }
    
    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getDestinationIban() {
        return destinationIban;
    }
    
    public void setDestinationIban(String destinationIban) {
        this.destinationIban = destinationIban;
    }
    
    /**
     * Vérifie si l'objet contient au moins un critère de recherche
     * @return true si au moins un critère est défini, false sinon
     */
    public boolean hasAnyCriteria() {
        return batchId != null || 
               startDate != null || 
               endDate != null || 
               destinationIban != null;
    }
} 