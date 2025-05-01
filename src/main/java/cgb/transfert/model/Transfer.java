package cgb.transfert.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Data
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String sourceIban;
    private String destinationIban;
    private Double amount;
    private LocalDate transferDate;
    private String description;

    // Getters and Setters with lombok
    
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public LocalDate getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(LocalDate transferDate) {
		this.transferDate = transferDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	// Méthodes de compatibilité pour l'ancien code
    @Deprecated
    public String getSourceAccountNumber() {
        return getSourceIban();
    }
    
    @Deprecated
    public void setSourceAccountNumber(String accountNumber) {
        setSourceIban(accountNumber);
    }
    
    @Deprecated
    public String getDestinationAccountNumber() {
        return getDestinationIban();
    }
    
    @Deprecated
    public void setDestinationAccountNumber(String accountNumber) {
        setDestinationIban(accountNumber);
    }
} 