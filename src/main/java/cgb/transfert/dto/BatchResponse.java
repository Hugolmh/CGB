package cgb.transfert.dto;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse de création d'un lot de virements
 */
public class BatchResponse {
    private Long numLot;
    private LocalDateTime dateLancement;
    private String message;
    private String etat;

    public BatchResponse() {
    }

    public BatchResponse(Long numLot, LocalDateTime dateLancement, String message, String etat) {
        this.numLot = numLot;
        this.dateLancement = dateLancement;
        this.message = message;
        this.etat = etat;
    }

    // Getters and Setters
    public Long getNumLot() {
        return numLot;
    }

    public void setNumLot(Long numLot) {
        this.numLot = numLot;
    }

    public LocalDateTime getDateLancement() {
        return dateLancement;
    }

    public void setDateLancement(LocalDateTime dateLancement) {
        this.dateLancement = dateLancement;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
} 