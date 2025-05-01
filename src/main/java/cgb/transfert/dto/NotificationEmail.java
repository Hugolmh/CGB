package cgb.transfert.dto;

import java.time.LocalDateTime;

/**
 * DTO pour les notifications par email
 */
public class NotificationEmail {
    private String recipient;
    private String subject;
    private String body;
    
    // Constructeurs
    public NotificationEmail() {
    }
    
    public NotificationEmail(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }
    
    // Getters et setters
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
} 