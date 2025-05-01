package cgb.transfert.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité spécifique pour les tests
 * Cette configuration désactive la protection CSRF pour faciliter les tests
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Désactive CSRF pour les tests
            .authorizeHttpRequests(
                    (requests) -> requests.anyRequest().authenticated()
                    )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
} 