package com.geoquiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conquered_countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConqueredCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    private LocalDateTime conqueredAt;
    
    private int attempts;
    
    private String gameMode; // Globle, Flaggle, Worldle
}
