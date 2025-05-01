package cgb.transfert.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;

/**
 * Entité représentant un transfert bancaire faisant partie d'un lot
 */
@Entity
@Data
public class BatchTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sourceIban;
    private String destinationIban;
    private Double amount;
    private LocalDate transferDate;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private TransferStatus status;
    
    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonBackReference
    private TransferBatch batch;
    
    /**
     * Enumération des statuts possibles pour un transfert
     */
    public enum TransferStatus {
        ATTENTE,
        ECHEC,
        SUCCESS,
        CANCELED
    }
} 