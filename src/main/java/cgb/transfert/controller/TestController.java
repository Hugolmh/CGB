package cgb.transfert.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
	
	@GetMapping("/{id}")
	public String obtenirTache(@PathVariable int id) {
		return "Recu : " + id;
	}
	
	@GetMapping("/")
	public String testVide() {
		return "Racine sous test";
	}
} 