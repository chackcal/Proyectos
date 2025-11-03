package com.santi.linea.config;

import com.santi.linea.models.Puesto;
import com.santi.linea.repositories.PuestoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner initPuestos(PuestoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(Puesto.builder().nombre("P1_EnsambleBase").ordenSecuencia(1).build());
                repo.save(Puesto.builder().nombre("P2_MontajeComponentes").ordenSecuencia(2).build());
                repo.save(Puesto.builder().nombre("P3_ControlCalidad").ordenSecuencia(3).build());
            }
        };
    }
}
