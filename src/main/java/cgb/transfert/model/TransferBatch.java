package cgb.transfert.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un lot de virements bancaires
 */
@Entity
@Data
public class TransferBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime creationDate;
    
    @Enumerated(EnumType.STRING)
    private BatchStatus status;
    
    @Column(unique = true)
    private String batchReference;
    
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BatchTransfer> transfers = new ArrayList<>();
    
    /**
     * Ajoute un transfert au lot
     * @param transfer Le transfert à ajouter
     */
    public void addTransfer(BatchTransfer transfer) {
        transfers.add(transfer);
        transfer.setBatch(this);
    }
    
    /**
     * Enumération des statuts possibles pour un lot
     */
    public enum BatchStatus {
        EN_COURS,
        TERMINE,
        ECHEC
    }
} 