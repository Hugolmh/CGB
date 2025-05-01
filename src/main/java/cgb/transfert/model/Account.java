package cgb.transfert.model;

import jakarta.persistence.*;
import lombok.Data;

import cgb.transfert.exception.ExceptionInvalidIbanFormat;
import cgb.transfert.exception.ExceptionInvalidUnCheckableIban;
import cgb.transfert.util.CGBIbanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Entity
@Data
public class Account {
    @Id
    @Column(name = "accountNumber")
    private String iban;
	private Double solde;
    
    // Le validateur d'IBAN est utilisé dans setIban(), mais nous ne pouvons pas utiliser @Autowired dans une entité
    // Nous utiliserons plutôt le service statique pour la compatibilité
    
    /**
     * Constructeur par défaut
     */
    public Account() {
    }
    
    /**
     * Constructeur avec IBAN (validé automatiquement)
     * @param iban L'IBAN du compte
     * @param solde Le solde initial
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     * @throws ExceptionInvalidUnCheckableIban si l'IBAN est invalide
     */
    public Account(String iban, Double solde) throws ExceptionInvalidIbanFormat, ExceptionInvalidUnCheckableIban {
        setIban(iban);
        this.solde = solde;
    }

    // Getters and Setters
	public Double getSolde() {
		return solde;
	}
	
	public void setSolde(Double solde) {
		this.solde = solde;
	}
	
    public String getIban() {
		return iban;
	}
    
	/**
     * Définit l'IBAN du compte après validation
     * @param iban L'IBAN à définir
     * @throws ExceptionInvalidIbanFormat si le format de l'IBAN est incorrect
     * @throws ExceptionInvalidUnCheckableIban si l'IBAN est invalide
     */
    public void setIban(String iban) throws ExceptionInvalidIbanFormat, ExceptionInvalidUnCheckableIban {
        // Valider l'IBAN avant de l'affecter
        // Nous devons utiliser la méthode statique car @Autowired ne fonctionne pas dans une entité JPA
        CGBIbanValidator.getInstanceValidator().validateIban(iban);
        this.iban = iban;
    }
    
    // Méthodes de compatibilité pour l'ancien code
    @Deprecated
    public String getAccountNumber() {
        return getIban();
    }
    
    @Deprecated
    public void setAccountNumber(String accountNumber) {
        try {
            setIban(accountNumber);
        } catch (ExceptionInvalidIbanFormat | ExceptionInvalidUnCheckableIban e) {
            // Pour maintenir la compatibilité, on ne propage pas les exceptions
            // Mais on log l'erreur
            System.err.println("Avertissement: tentative de définir un IBAN invalide: " + e.getMessage());
        }
    }
} 