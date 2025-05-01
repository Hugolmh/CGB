package cgb.transfert.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cgb.transfert.dto.NotificationEmail;

/**
 * Service pour l'envoi d'emails de notification
 */
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Adresse email de l'expéditeur
     */
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Envoie un email de notification
     * @param notificationEmail Les détails de l'email à envoyer
     */
    public void sendEmail(NotificationEmail notificationEmail) {
        System.out.println("Tentative d'envoi d'email à " + notificationEmail.getRecipient());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notificationEmail.getRecipient());
            message.setSubject(notificationEmail.getSubject());
            message.setText(notificationEmail.getBody());
            
            System.out.println("Configuration de l'email terminée, envoi en cours...");
            mailSender.send(message);
            
            // Log pour le débogage
            System.out.println("==================================");
            System.out.println("Email envoyé avec succès :");
            System.out.println("De: " + fromEmail);
            System.out.println("À: " + notificationEmail.getRecipient());
            System.out.println("Sujet: " + notificationEmail.getSubject());
            System.out.println("==================================");
        } catch (Exception e) {
            System.err.println("==================================");
            System.err.println("Erreur lors de l'envoi de l'email :");
            System.err.println("De: " + fromEmail);
            System.err.println("À: " + notificationEmail.getRecipient());
            System.err.println("Sujet: " + notificationEmail.getSubject());
            System.err.println("Erreur: " + e.getMessage());
            System.err.println("==================================");
            e.printStackTrace();
        }
    }
} 