package cgb.transfert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Classe principale de l'application de transfert bancaire
 * Point d'entrée de l'application Spring Boot
 */
@SpringBootApplication
@EnableAsync
public class ServerTransferApp {

	public static void main(String[] args) {
		SpringApplication.run(ServerTransferApp.class, args);    	
		// TODO Auto-generated method stub	
		//Tester chargement...

	}
}

